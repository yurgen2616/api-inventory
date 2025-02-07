package com.drogueria.inventario.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drogueria.inventario.models.Distributor;
import com.drogueria.inventario.services.DistributorService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/distributors")
public class DistributorController {

    @Autowired
    private DistributorService distributorService;

    @GetMapping
    public List<Distributor> getAllDistributors() {
        return distributorService.getAllDistributors();
    }

    @GetMapping("/{id}")
    public Distributor getDistributorById(@PathVariable Long id) {
        return distributorService.getDistributorById(id);
    }

    @PostMapping
    public Distributor createDistributor(@Valid @RequestBody Distributor distributor) {
        return distributorService.createDistributor(distributor);
    }

    @PutMapping("/{id}")
    public Distributor updateDistributor(@Valid @PathVariable Long id, @RequestBody Distributor distributor) {
        return distributorService.updateDistributor(id, distributor);
    }

    @DeleteMapping("/{id}")
    public void deleteDistributor(@PathVariable Long id) {
        distributorService.deleteDistributor(id);
    }
}
