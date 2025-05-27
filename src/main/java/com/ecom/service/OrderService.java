package com.ecom.service;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    public void saveProduct(Integer userId , OrderRequest orderRequest);

    public List<ProductOrder> getOrdersByUser(Integer userId);

    public Boolean updateStatus(Integer id, String status);

    public List<ProductOrder> getAllOrders();
}
