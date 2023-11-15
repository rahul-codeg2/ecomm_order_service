package com.ecom_order_service.controller;

import com.ecom_order_service.model.OrderRequest;
import com.ecom_order_service.model.Orders;
import com.ecom_order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ecomm")
public class OrderController
{
    @Autowired
    private OrderService orderService;
    @PostMapping("/home/place-order")
    public ResponseEntity<Orders> placeOrder(@RequestBody OrderRequest orderRequest)
    {
        return orderService.placeOrder(orderRequest);

    }

    @GetMapping("/home/get-all-orders")
    public ResponseEntity <List<Orders>> getAllOrders()
    {
        return orderService.getAllOrders();

    }
}
