package com.drogueria.inventario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drogueria.inventario.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

