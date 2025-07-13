package com.shopsphere.service;

import com.shopsphere.dto.CategoryRequest;
import com.shopsphere.dto.CategoryResponse;
import com.shopsphere.model.Category;
import com.shopsphere.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private CategoryResponse convertToDto(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        return dto;
    }

    private Category convertToEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(String categoryId) { // Return type changed to Category
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId));
    }

    // Public method for Controller to get CategoryResponse by ID
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponseById(String categoryId) {
        return convertToDto(getCategoryById(categoryId)); // Calls the entity-returning method and converts
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with name: " + name));
        return convertToDto(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Basic validation (e.g., uniqueness)
        if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with name '" + request.getName() + "' already exists.");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(String categoryId, CategoryRequest request) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId));

        // Check for name uniqueness if name is changed
        if (!existingCategory.getName().equalsIgnoreCase(request.getName()) && categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with name '" + request.getName() + "' already exists.");
        }

        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(String categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId));

        if (!categoryToDelete.getProducts().isEmpty()) {
            // IMPORTANT: Since products are LAZY, this line will trigger loading them if they haven't been.
            // It might cause LazyInitializationException if not called within a transaction!
            // Because deleteCategory is @Transactional, it's safe here.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete category with associated products. Reassign or delete products first.");
        }
        categoryRepository.delete(categoryToDelete);
    }
}