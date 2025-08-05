package com.ecom.service.Impl;


import com.ecom.model.Product;
import com.ecom.repository.ProductRepo;
import com.ecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    public Page<Product> getAllProductsPagination(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepo.findAll(pageable);
    }


    @Override
    public Boolean deleteProduct(int id) {
        Product product = productRepo.findById(id).orElse(null);
        if (!ObjectUtils.isEmpty(product)) {
            productRepo.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(int id) {
        Product product = productRepo.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductById(product.getId());

        String imageName = image.isEmpty() ? dbProduct.getImageName() : image.getOriginalFilename();
        dbProduct.setTitle(product.getTitle());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setImageName(imageName);
        dbProduct.setIsActive(product.getIsActive());

        dbProduct.setDiscount(product.getDiscount());
        Double discount = product.getPrice() * (product.getDiscount() / 100.0);
        Double discountPrice = product.getPrice() - discount;
        dbProduct.setDiscountPrice(discountPrice);

        Product updateProduct = productRepo.save(dbProduct);
        if (!ObjectUtils.isEmpty(updateProduct)) {
            if (!image.isEmpty()) {
                try {
                    File saveFile = new ClassPathResource("/static/img").getFile();
                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());

                    System.out.println(path);
                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return product;
        }
        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products = null;
        if (ObjectUtils.isEmpty(category)) {
            products = productRepo.findByIsActiveTrue();
        } else {
            products = productRepo.findByCategory(category);
        }

        return products;
    }

    @Override
    public List<Product> searchProduct(String text) {
        return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(text, text);
    }

    @Override
    public Page<Product> searchProductPagination(int pageNo, int pageSize, String text) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(text, text, pageable);
    }




    @Override
    public Page<Product> getAllActiveProductPagination(int pageNo, int pageSize, String category) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);


        Page<Product> pageProduct = null;
        if (ObjectUtils.isEmpty(category)) {
            pageProduct = productRepo.findByIsActiveTrue(pageable);
        } else {
            pageProduct = productRepo.findByCategory(pageable, category);
        }

        return pageProduct;
    }


    @Override
    public Page<Product> searchActiveProductPagination(int pageNo, int pageSize, String category,String text) {

        Page<Product> pageProduct = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        pageProduct = productRepo.findByIsActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(text, text, pageable);

//        if (ObjectUtils.isEmpty(category)) {
//            pageProduct = productRepo.findByIsActiveTrue(pageable);
//        } else {
//            pageProduct = productRepo.findByCategory(pageable, category);
//        }

        return pageProduct;
    }


}
