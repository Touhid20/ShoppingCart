package com.ecom.repository;

import com.ecom.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderRepo extends JpaRepository<ProductOrder,Integer> {
}
