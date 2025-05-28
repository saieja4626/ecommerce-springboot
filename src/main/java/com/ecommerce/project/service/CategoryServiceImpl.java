package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()){
            throw new APIException("No Category is present !");
        }

        List<CategoryDTO> categoryDTO = new ArrayList<>();
        for(int i=0; i<categories.size(); i++){
            CategoryDTO dto = modelMapper.map(categories.get(i), CategoryDTO.class);
            categoryDTO.add(dto);

        }
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTO);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category existingCategory = categoryRepository.findBycategoryName(category.getCategoryName());
         if(existingCategory != null){
             throw new APIException("Category with the name "+ category.getCategoryName()+ " already exists !!!");
         }
       Category category1 =  categoryRepository.save(category);
         CategoryDTO savedCategoryDTO = modelMapper.map(category1, CategoryDTO.class);
         return savedCategoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (!optionalCategory.isPresent()) {

            throw new ResourceNotFoundException("category","categoryId",categoryId);

        }
        Category category = optionalCategory.get();
        categoryRepository.delete(optionalCategory.get());
        CategoryDTO categoryDTO = modelMapper.map(category,CategoryDTO.class);
        return categoryDTO;

    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        Category updatedCategory = categoryRepository.findById(categoryId)
                .map(existingCategory -> {
                    existingCategory.setCategoryName(category.getCategoryName());
                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryId",categoryId));

        CategoryDTO updatedDTO = modelMapper.map(updatedCategory,CategoryDTO.class);

        return updatedDTO;
    }



}
