package com.drogueria.inventario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drogueria.inventario.models.Distributor;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
}
