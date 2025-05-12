package com.ecom.service.Impl;


import com.ecom.model.Product;
import com.ecom.repository.ProductRepo;
import com.ecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Override
    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Boolean deleteProduct(int id) {
       Product product = productRepo.findById(id).orElse(null);
       if(!ObjectUtils.isEmpty(product)){
           productRepo.delete(product);
           return true;
       }
        return false;
    }


}
