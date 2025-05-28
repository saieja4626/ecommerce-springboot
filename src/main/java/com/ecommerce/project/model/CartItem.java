package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;


    @ManyToOne
    @JoinColumn(name = "cart_id")
    @ToString.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    private Integer quantity;
    private double discount;
    private double productPrice;
}
