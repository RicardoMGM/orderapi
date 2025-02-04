package br.com.ambev.orderapi.queue;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.model.Product;
import br.com.ambev.orderapi.model.enums.OrderStatus;
import br.com.ambev.orderapi.repository.OrderRepository;
import br.com.ambev.orderapi.service.OrderCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final OrderCacheService orderCacheService;

    public OrderConsumer(OrderRepository orderRepository, OrderCacheService orderCacheService) {
        this.orderRepository = orderRepository;
        this.orderCacheService = orderCacheService;
    }

    @RabbitListener(queues = "order-queue")
    public void processOrder(Order order) {
        log.info("Recebendo pedido da fila: " + order.getOrderId());

        // Pausa por 1 minuto
        try {
            Thread.sleep(60000); // 1 minuto em milissegundos
        } catch (InterruptedException e) {
            log.error("Erro durante a pausa: " + e.getMessage());
        }

        if (order.getProducts() != null) {
            Double total = 0.0;
            for (Product product : order.getProducts()) {
                product.setOrder(order); // Associa os produtos ao pedido
                total += product.getPrice(); // Faz o somatório total dos pedidos.
            }
            order.setTotal(total);
        }

        order.setStatus(OrderStatus.PROCESSADO);

        // Salvar pedido no banco de dados PostgreSQL
        orderRepository.save(order);
        log.info("Pedido salvo no banco: " + order.getOrderId());

        // Atualizar a ordem no Redis
        orderCacheService.updateOrderInCache(order.getOrderId(), order);
    }
}
