package com.drogueria.inventario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drogueria.inventario.models.SalesForm;

public interface SalesFormRepository extends JpaRepository<SalesForm, Long> {
}
