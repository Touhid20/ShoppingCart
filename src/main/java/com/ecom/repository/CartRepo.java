package com.ecom.repository;

import com.ecom.model.Cart;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepo extends JpaRepository<Cart,Integer> {

    public Cart findByProductIdAndUserId(Integer productId, Integer userId);
}
