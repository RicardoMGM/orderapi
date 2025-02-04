package br.com.ambev.orderapi.service;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderCacheService orderCacheService;

    public OrderService(OrderRepository orderRepository, OrderCacheService orderCacheService) {
        this.orderRepository = orderRepository;
        this.orderCacheService = orderCacheService;
    }

    public Order getOrder(String orderId) {

        // Tentar buscar no Redis primeiro
        Order order = orderCacheService.getOrderFromCache(orderId);

        if (Objects.nonNull(order)) {
            log.info("Pedido encontrado no Redis: " + orderId);
            return order;
        }

        // Se não encontrou no Redis, buscar no PostgreSQL
        Optional<Order> orderFromDB = orderRepository.findByOrderId(orderId);
        if (orderFromDB.isPresent()) {
            log.info("Pedido encontrado no Banco de Dados: " + orderId);

            // Atualizar Redis para futuras consultas
            orderCacheService.updateOrderInCache(orderId, orderFromDB.get());
            return orderFromDB.get();
        } else {
            log.info("Pedido não encontrado: " + orderId);
            return null;
        }
    }

    public boolean verificarPedidoExiste(String orderId){
        // Verifica no Redis se o pedido já existe
        if(orderCacheService.existsInCache(orderId)){
            log.info("Pedido encontrado no Redis: " + orderId);
            return true;
        }else{
            // Verifica se o ID do pedido começa com a data atual (AnoMesDia)
            LocalDate currentDate = LocalDate.now();
            if (!orderId.startsWith(currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))) {
                // Se o pedido não for do dia atual, verifica no banco de dados
                return orderRepository.findByOrderId(orderId).isPresent();
            }
            return false;
        }
    }
}
