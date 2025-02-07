package com.drogueria.inventario.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drogueria.inventario.models.Distributor;
import com.drogueria.inventario.repositories.DistributorRepository;
import com.drogueria.inventario.services.DistributorService;

import java.util.List;

@Service
public class DistributorServiceImpl implements DistributorService {

    @Autowired
    private DistributorRepository distributorRepository;

    @Override
    public Distributor createDistributor(Distributor distributor) {
        return distributorRepository.save(distributor);
    }

    @Override
    public Distributor updateDistributor(Long id, Distributor distributor) {
        Distributor existingDistributor = distributorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Distributor not found"));
        existingDistributor.setName(distributor.getName());
        return distributorRepository.save(existingDistributor);
    }

    @Override
    public void deleteDistributor(Long id) {
        distributorRepository.deleteById(id);
    }

    @Override
    public Distributor getDistributorById(Long id) {
        return distributorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Distributor not found"));
    }

    @Override
    public List<Distributor> getAllDistributors() {
        return distributorRepository.findAll();
    }
}
