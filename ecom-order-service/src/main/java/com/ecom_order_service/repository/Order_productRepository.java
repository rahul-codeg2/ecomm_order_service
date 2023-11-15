package com.ecom_order_service.repository;

import com.ecom_order_service.model.Order_product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Order_productRepository extends JpaRepository<Order_product,Integer> {
}
