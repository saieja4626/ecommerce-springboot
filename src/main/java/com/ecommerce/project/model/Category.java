package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long categoryId;
    @NotBlank
    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @ToString.Exclude
   private List<Product> products;



}
