package com.ecom.service;


import com.ecom.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    public Product saveProduct(Product product);

    public List<Product> getAllProducts();

    public Boolean deleteProduct(int id);

    public Product getProductById(int id);

    public  Product updateProduct(Product product , MultipartFile file);

    public List<Product> getAllActiveProducts(String category);

    public List<Product> searchProduct(String text);

    public Page<Product> getAllActiveProductPagination(int pageNo, int pageSize,String category);

    public Page<Product> searchProductPagination(int pageNo,int pageSize,String text);

    public Page<Product> getAllProductsPagination(int pageNo,int pageSize);

}
