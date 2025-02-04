package br.com.ambev.orderapi.model;

import br.com.ambev.orderapi.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Schema(hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo orderId é obrigatório.")
    @Column(nullable = false, unique = true)
    private String orderId;

    @Schema(hidden = true)
    @Column(nullable = false)
    private Double total;

    @Schema(hidden = true)
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull(message = "O campo status é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @NotNull(message = "Não foi informado os produtos do Pedido.")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

}
