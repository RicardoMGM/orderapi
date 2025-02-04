package br.com.ambev.orderapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Schema(hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo productId é obrigatório.")
    @Column(nullable = false)
    private String productId;

    @NotBlank(message = "O campo name é obrigatório.")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "O price é obrigatório.")
    @Column(nullable = false)
    private Double price;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

}
