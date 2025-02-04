package br.com.ambev.orderapi.service;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCacheService orderCacheService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderId("20250203123456");
    }

    @Test
    @DisplayName("Retornar Order quando ela existe no cache")
    void getOrderQuandoPedidoExisteNoCache() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(testOrder);

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(orderRepository, never()).findByOrderId(anyString());
    }

    @Test
    @DisplayName("Retornar Order quando ela não existe no cache, mas existe no banco")
    void getOrderQuandoPedidoNaoEstaNoCacheMasExisteNoBD() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(null);
        when(orderRepository.findByOrderId(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(orderCacheService).updateOrderInCache(testOrder.getOrderId(), testOrder);
    }

    @Test
    @DisplayName("Retornar null quando a Order não existe no cache, e não existe no banco")
    void getOrderQuandoPedidoNaoExisteEmNenhumLugar() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(null);
        when(orderRepository.findByOrderId(testOrder.getOrderId())).thenReturn(Optional.empty());

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNull(result);
    }

    @Test
    @DisplayName("Verifica se Order existe no cache.")
    void verificarPedidoExisteNoCache() {
        when(orderCacheService.existsInCache(testOrder.getOrderId())).thenReturn(true);

        boolean result = orderService.verificarPedidoExiste(testOrder.getOrderId());

        assertTrue(result);
        verify(orderRepository, never()).findByOrderId(anyString());
    }

    @Test
    @DisplayName("Verifica se Order existe, caso não tenha no cache, o pedido não seja de hoje, mas exista no banco.")
    void verificarPedidoExisteQuandoOrderNaoEstaNoCacheENaoDeHojeMasExisteNoBD() {
        String oldOrderId = "20240101999999";
        when(orderCacheService.existsInCache(oldOrderId)).thenReturn(false);
        when(orderRepository.findByOrderId(oldOrderId)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.verificarPedidoExiste(oldOrderId);

        assertTrue(result);
    }

    @Test
    @DisplayName("Verifica se Order existe, caso não tenha no cache, o pedido não seja de hoje, e não exista no banco.")
    void verificarPedidoExisteQuandoOrderNaoNoCacheEnaoEDeHojeENaoExisteNoBD() {
        String oldOrderId = "20240101999999";
        when(orderCacheService.existsInCache(oldOrderId)).thenReturn(false);
        when(orderRepository.findByOrderId(oldOrderId)).thenReturn(Optional.empty());

        boolean result = orderService.verificarPedidoExiste(oldOrderId);

        assertFalse(result);
    }
}