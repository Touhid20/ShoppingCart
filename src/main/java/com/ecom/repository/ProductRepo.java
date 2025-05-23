package com.ecom.repository;

import com.ecom.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product,Integer> {


    List<Product> findByIsActiveTrue();

    List<Product> findByCategory(String category);

}
