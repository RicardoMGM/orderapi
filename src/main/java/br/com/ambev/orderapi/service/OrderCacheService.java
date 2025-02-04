package br.com.ambev.orderapi.service;

import br.com.ambev.orderapi.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public OrderCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // Salvar pedido no Redis com expiração de 1 dia
    public void saveOrderToCache(Order order) {
        try {
            String jsonOrder = objectMapper.writeValueAsString(order);
            redisTemplate.opsForValue().set(order.getOrderId(), jsonOrder, 1, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter pedido para JSON." + e.getMessage());
            throw new RuntimeException("Erro ao converter pedido para JSON.", e);
        }
    }

    // Buscar pedido do Redis
    public Order getOrderFromCache(String orderId) {
        try {
            String jsonOrder = redisTemplate.opsForValue().get(orderId);
            return jsonOrder != null ? objectMapper.readValue(jsonOrder, Order.class) : null;
        } catch (JsonProcessingException e) {
            log.error("Erro ao desserializar pedido do Redis: " + e.getMessage());
            throw new RuntimeException("Erro ao desserializar pedido do Redis.", e);
        }
    }

    // Atualizar um pedido existente no Redis
    public void updateOrderInCache(String orderId, Order order) {
        try {
            String jsonOrder = objectMapper.writeValueAsString(order);
            redisTemplate.opsForValue().set(orderId, jsonOrder, 1, TimeUnit.DAYS);
            log.info("Pedido atualizado no Redis: " + orderId);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar JSON para salvar no Redis: " + e.getMessage());
            throw new RuntimeException("Erro ao serializar JSON para salvar no Redis: " + e.getMessage());
        }
    }

    public boolean existsInCache(String orderId) {
        return redisTemplate.hasKey(orderId);
    }

}
