package com.drogueria.inventario.services;


import java.util.List;

import com.drogueria.inventario.models.SalesForm;

public interface SalesFormService {
    SalesForm createSalesForm(SalesForm salesForm);
    SalesForm updateSalesForm(Long id, SalesForm salesForm);
    void deleteSalesForm(Long id);
    SalesForm getSalesFormById(Long id);
    List<SalesForm> getAllSalesForms();
}
