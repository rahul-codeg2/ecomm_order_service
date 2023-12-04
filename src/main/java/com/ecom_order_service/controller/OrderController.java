package com.ecom_order_service.controller;

import com.ecom_order_service.dto.OrderRequest;
import com.ecom_order_service.model.Orders;
import com.ecom_order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController
{
    @Autowired
    private OrderService orderService;
    @PostMapping("/place-order")
    public ResponseEntity<Orders> placeOrder(@RequestBody OrderRequest orderRequest,@RequestHeader("Authorization") String token)
    {
        return orderService.placeOrder(orderRequest,token);

    }

    @GetMapping("/orders")
    public ResponseEntity <List<Orders>> getAllOrders(@RequestHeader("Authorization") String token)
    {
        return orderService.getAllOrders(token);

    }
}
