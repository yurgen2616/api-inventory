package com.drogueria.inventario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drogueria.inventario.models.SaleDetail;


@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {
}
