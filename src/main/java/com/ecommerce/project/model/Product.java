package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    @NotBlank
    @Size(min = 3, message = "product names must atleast 3 characters")
    private String productName;
    private String image;
    private Integer quantity;
    @NotBlank
    @Size(min = 6, message = "product description must atleast 6 characters")
    private String description;
    private double price;
    private double discount;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST,CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<CartItem> products = new ArrayList<>();
}
