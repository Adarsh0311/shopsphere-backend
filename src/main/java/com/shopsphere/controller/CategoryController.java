package com.shopsphere.controller;

import com.shopsphere.dto.CategoryResponse;
import com.shopsphere.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryResponseById(id);
        return ResponseEntity.ok(category);
    }


    @GetMapping("/search/name")
    public ResponseEntity<CategoryResponse> getCategoryByName(@RequestParam String name) {
        CategoryResponse category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }
}