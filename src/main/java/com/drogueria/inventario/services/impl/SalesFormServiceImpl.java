package com.drogueria.inventario.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drogueria.inventario.models.SalesForm;
import com.drogueria.inventario.repositories.SalesFormRepository;
import com.drogueria.inventario.services.SalesFormService;

import java.util.List;

@Service
public class SalesFormServiceImpl implements SalesFormService {

    @Autowired
    private SalesFormRepository salesFormRepository;

    @Override
    public SalesForm createSalesForm(SalesForm salesForm) {
        return salesFormRepository.save(salesForm);
    }

    @Override
    public SalesForm updateSalesForm(Long id, SalesForm salesForm) {
        SalesForm existingSalesForm = salesFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Form not found"));
        existingSalesForm.setName(salesForm.getName());
        return salesFormRepository.save(existingSalesForm);
    }

    @Override
    public void deleteSalesForm(Long id) {
        salesFormRepository.deleteById(id);
    }

    @Override
    public SalesForm getSalesFormById(Long id) {
        return salesFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Form not found"));
    }

    @Override
    public List<SalesForm> getAllSalesForms() {
        return salesFormRepository.findAll();
    }
}
