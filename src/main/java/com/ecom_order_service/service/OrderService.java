package com.ecom_order_service.service;

import com.ecom_order_service.dto.OrderRequest;
import com.ecom_order_service.model.Order_product;
import com.ecom_order_service.model.Orders;
import com.ecom_order_service.dto.ProductStockResponse;
import com.ecom_order_service.repository.OrderRepository;
import com.ecom_order_service.repository.Order_productRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.List;
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

    @Value("${jwt.secret-key}")
    private String secret;

    public ResponseEntity<Orders> placeOrder(OrderRequest orderRequest,String token)
    {
        Claims claims=validateJwtToken(token);
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
            new_order.setUser_id((int)claims.get("userId"));
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
    public ResponseEntity<List<Orders>> getAllOrders(String token)
    {
        validateJwtToken(token);
        return new ResponseEntity<>(orderRepository.findAll(),HttpStatus.OK);
    }
    public Claims validateJwtToken(String jwtToken) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken.substring(7)).getBody();

            // Extract user details from claims
            String email = claims.getSubject();
            int user_id = (int) claims.get("userId");
            Date expirationDate = claims.getExpiration();
            if (expirationDate != null && expirationDate.before(new Date())) {
                // Token has expired
                throw new RuntimeException("JWT token has expired");
            }
            return  claims;

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }
}
