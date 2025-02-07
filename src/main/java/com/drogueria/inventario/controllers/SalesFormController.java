package com.drogueria.inventario.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drogueria.inventario.models.SalesForm;
import com.drogueria.inventario.services.SalesFormService;

import java.util.List;

@RestController
@RequestMapping("/sales-forms")
public class SalesFormController {

    @Autowired
    private SalesFormService salesFormService;

    @GetMapping
    public List<SalesForm> getAllSalesForms() {
        return salesFormService.getAllSalesForms();
    }

    @GetMapping("/{id}")
    public SalesForm getSalesFormById(@PathVariable Long id) {
        return salesFormService.getSalesFormById(id);
    }

    @PostMapping
    public SalesForm createSalesForm(@RequestBody SalesForm salesForm) {
        return salesFormService.createSalesForm(salesForm);
    }

    @PutMapping("/{id}")
    public SalesForm updateSalesForm(@PathVariable Long id, @RequestBody SalesForm salesForm) {
        return salesFormService.updateSalesForm(id, salesForm);
    }

    @DeleteMapping("/{id}")
    public void deleteSalesForm(@PathVariable Long id) {
        salesFormService.deleteSalesForm(id);
    }
}
