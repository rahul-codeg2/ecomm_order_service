package com.ecom_order_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse
{
    private int product_id;
    private int available_stock;
    private double price;
}
