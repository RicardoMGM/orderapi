package br.com.ambev.orderapi.service;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
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
    void getOrderQuandoPedidoExisteNoCache() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(testOrder);

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(orderRepository, never()).findByOrderId(anyString());
    }

    @Test
    void getOrderQuandoPedidoNaoEstaNoCacheMasExisteNoBD() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(null);
        when(orderRepository.findByOrderId(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(orderCacheService).updateOrderInCache(testOrder.getOrderId(), testOrder);
    }

    @Test
    void getOrderQuandoPedidoNaoExisteEmNenhumLugar() {
        when(orderCacheService.getOrderFromCache(testOrder.getOrderId())).thenReturn(null);
        when(orderRepository.findByOrderId(testOrder.getOrderId())).thenReturn(Optional.empty());

        Order result = orderService.getOrder(testOrder.getOrderId());

        assertNull(result);
    }

    @Test
    void verificarPedidoExisteNoCache() {
        when(orderCacheService.existsInCache(testOrder.getOrderId())).thenReturn(true);

        boolean result = orderService.verificarPedidoExiste(testOrder.getOrderId());

        assertTrue(result);
        verify(orderRepository, never()).findByOrderId(anyString());
    }

    @Test
    void verificarPedidoExisteQuandoOrderNaoEstaNoCacheENaoDeHojeMasExisteNoBD() {
        String oldOrderId = "20240101999999";
        when(orderCacheService.existsInCache(oldOrderId)).thenReturn(false);
        when(orderRepository.findByOrderId(oldOrderId)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.verificarPedidoExiste(oldOrderId);

        assertTrue(result);
    }

    @Test
    void verificarPedidoExisteQuandoOrderNaoNoCacheEnaoEDeHojeMasExisteNoBD() {
        String oldOrderId = "20240101999999";
        when(orderCacheService.existsInCache(oldOrderId)).thenReturn(false);
        when(orderRepository.findByOrderId(oldOrderId)).thenReturn(Optional.empty());

        boolean result = orderService.verificarPedidoExiste(oldOrderId);

        assertFalse(result);
    }
}