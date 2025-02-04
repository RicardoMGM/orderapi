package br.com.ambev.orderapi.service;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.model.enums.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderCacheService orderCacheService;

    private Order order;
    private final String orderJson = "{\"orderId\":\"202402031234\",\"total\":100.0,\"status\":\"PENDENTE\"}";

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        order = new Order();
        order.setOrderId("20240203-1234");
        order.setTotal(100.0);
        order.setStatus(OrderStatus.PENDENTE);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(order)).thenReturn(orderJson);
        when(objectMapper.readValue(orderJson, Order.class)).thenReturn(order);
    }

    @Test
    @DisplayName("Salvar pedido no cache")
    void salvarPedidoNoCache() {
        orderCacheService.saveOrderToCache(order);

        verify(valueOperations, times(1)).set(order.getOrderId(), orderJson, 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("Buscar pedido do cache")
    void buscarPedidoDoCache() {
        when(valueOperations.get(order.getOrderId())).thenReturn(orderJson);

        Order cachedOrder = orderCacheService.getOrderFromCache(order.getOrderId());

        assertNotNull(cachedOrder);
        assertEquals(order.getOrderId(), cachedOrder.getOrderId());
    }

    @Test
    @DisplayName("Atualizar pedido no cache")
    void atualizarPedidoNoCache() {
        orderCacheService.updateOrderInCache(order.getOrderId(), order);

        verify(valueOperations, times(1)).set(order.getOrderId(), orderJson, 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("Verificar se pedido existe no cache")
    void verificarPedidoNoCache() {
        when(redisTemplate.hasKey(order.getOrderId())).thenReturn(true);

        boolean exists = orderCacheService.existsInCache(order.getOrderId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("Erro ao salvar pedido no cache")
    void erroAoSalvarPedidoNoCache() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(order)).thenThrow(new JsonProcessingException("Erro") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderCacheService.saveOrderToCache(order));
        assertTrue(exception.getMessage().contains("Erro ao converter pedido para JSON."));
    }

    @Test
    @DisplayName("Erro ao desserializar pedido do cache")
    void erroAoDesserializarPedidoDoCache() throws JsonProcessingException {
        when(valueOperations.get(order.getOrderId())).thenReturn(orderJson);
        when(objectMapper.readValue(orderJson, Order.class)).thenThrow(new JsonProcessingException("Erro") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderCacheService.getOrderFromCache(order.getOrderId()));
        assertTrue(exception.getMessage().contains("Erro ao desserializar pedido do Redis."));
    }
}