package com.drogueria.inventario.services;

import java.util.List;

import com.drogueria.inventario.models.Category;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
}
