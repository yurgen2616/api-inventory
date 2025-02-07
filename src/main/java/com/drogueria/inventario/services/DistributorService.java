package com.drogueria.inventario.services;

import java.util.List;

import com.drogueria.inventario.models.Distributor;

public interface DistributorService {
    Distributor createDistributor(Distributor distributor);
    Distributor updateDistributor(Long id, Distributor distributor);
    void deleteDistributor(Long id);
    Distributor getDistributorById(Long id);
    List<Distributor> getAllDistributors();
}
