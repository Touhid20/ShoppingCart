package com.ecom.service;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    public void saveProduct(Integer userId , OrderRequest orderRequest);
}
