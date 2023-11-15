package com.ecom_order_service.service;

import com.ecom_order_service.model.OrderRequest;
import com.ecom_order_service.model.Order_product;
import com.ecom_order_service.model.Orders;
import com.ecom_order_service.model.ProductStockResponse;
import com.ecom_order_service.repository.OrderRepository;
import com.ecom_order_service.repository.Order_productRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Order_productRepository orderProductRepository;
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<Orders> placeOrder(OrderRequest orderRequest) {
        double total_amount = 0;
        Orders new_order = new Orders();
        List<ProductStockResponse> productListWithAvailableStock = checkProductStock(orderRequest.getProduct_ids());
        for (ProductStockResponse p : productListWithAvailableStock) {
            total_amount += p.getPrice();
        }
        try
        {
            new_order.setOrder_no(UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            new_order.setOrder_status("Success");
            new_order.setTotal_amount(total_amount);
            new_order.setQuantity(productListWithAvailableStock.size());
            new_order.setUser_id(orderRequest.getUser_id());
            orderRepository.save(new_order);


            productListWithAvailableStock.stream().forEach(u -> {
                Order_product orderProduct = new Order_product();
                orderProduct.setOrder_id(new_order.getOrder_id());
                orderProduct.setProduct_id(u.getProduct_id());
                orderProductRepository.save(orderProduct);
            });


        } catch (Exception e)
        {
            return ResponseEntity.badRequest().build();
        }
        reduceProductStock(productListWithAvailableStock);
        return new ResponseEntity<Orders>(new_order, HttpStatus.OK);


    }

    private void reduceProductStock(List<ProductStockResponse> productListWithAvailableStock) {
        String productServiceUrl = "http://localhost:9001/ecomm/home/reduce-stock";
        RequestEntity<List<ProductStockResponse>> requestEntity = new RequestEntity<>(productListWithAvailableStock, HttpMethod.POST, URI.create(productServiceUrl));

        ParameterizedTypeReference<List<ProductStockResponse>> responseType = new ParameterizedTypeReference<List<ProductStockResponse>>() {
        };


        ResponseEntity<List<ProductStockResponse>> responseEntity = restTemplate.exchange(requestEntity, responseType);

    }

    public List<ProductStockResponse> checkProductStock(List<Integer> product_ids) {

        String productServiceUrl = "http://localhost:9001/ecomm/home/check-stock";


        RequestEntity<List<Integer>> requestEntity = new RequestEntity<>(product_ids, HttpMethod.POST, URI.create(productServiceUrl));


        ParameterizedTypeReference<List<ProductStockResponse>> responseType = new ParameterizedTypeReference<List<ProductStockResponse>>() {
        };


        ResponseEntity<List<ProductStockResponse>> responseEntity = restTemplate.exchange(requestEntity, responseType);
        List<ProductStockResponse> productListWithAvailableStock = responseEntity.getBody().stream().filter(product -> product.getAvailable_stock() > 0).collect(Collectors.toList());

        return productListWithAvailableStock;
    }

    public ResponseEntity<List<Orders>> getAllOrders()
    {
        return new ResponseEntity<>(orderRepository.findAll(),HttpStatus.OK);
    }
}
