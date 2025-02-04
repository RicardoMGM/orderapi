package br.com.ambev.orderapi.controller;

import br.com.ambev.orderapi.model.Order;
import br.com.ambev.orderapi.service.OrderCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import br.com.ambev.orderapi.service.OrderService;

import java.time.LocalDateTime;
import java.util.Objects;

import static br.com.ambev.orderapi.config.RabbitMQConfig.ORDER_QUEUE;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Endpoints para gerenciamento de pedidos")
public class OrderController {

    private final OrderCacheService orderCacheService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;

    public OrderController(OrderCacheService orderCacheService, RabbitTemplate rabbitTemplate, OrderService orderService) {
        this.orderCacheService = orderCacheService;
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Processa e Armazena um Pedido", description = "Armazena um Pedido no Redis e Envia para a Fila do RabbitMQ, para Posteriomente ser processado e armazenado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados do pedido")
    })
    public ResponseEntity<String> createOrder(@Valid @RequestBody Order request) {


        if (orderService.verificarPedidoExiste(request.getOrderId())) {
            log.info("Pedido duplicado ID: " + request.getOrderId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Pedido duplicado ID: " + request.getOrderId());
        }

        request.setDate(LocalDateTime.now());

        orderCacheService.saveOrderToCache(request);

        rabbitTemplate.convertAndSend(ORDER_QUEUE, request);

        log.info("Pedido enviado para o Redis e para a Fila ID: " + request.getOrderId());
        return ResponseEntity.ok("Pedido enviado com sucesso ID: " + request.getOrderId());
    }

    // Rota para o Sistema B consultar uma Order
    @GetMapping("/{orderId}")
    @Operation(summary = "Consulta um pedido", description = "Busca um pedido no sistema, com seus dados e status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {

        Order order = orderService.getOrder(orderId);

        if (Objects.nonNull(order)) {
            return ResponseEntity.ok(order);
        } else {
            log.info("Pedido não encontrado: " + orderId);
            return ResponseEntity.status(404).body("Pedido não encontrado: " + orderId);
        }
    }
}
