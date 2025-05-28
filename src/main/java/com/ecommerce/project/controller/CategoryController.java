package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import com.ecommerce.project.service.CategoryServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api")
public class CategoryController {


    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER, required = false ) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
                                                             @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber,pageSize, sortBy, sortOrder);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> creatCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
       CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategoryDTO);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable long categoryId) {
       CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);

       return ResponseEntity.status(HttpStatus.OK).body(categoryDTO);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid  @RequestBody CategoryDTO categoryDTO,
                                                 @PathVariable long categoryId) {

            CategoryDTO category1 = categoryService.updateCategory(categoryDTO, categoryId);
            return ResponseEntity.status(HttpStatus.OK).body(category1);




    }

}


