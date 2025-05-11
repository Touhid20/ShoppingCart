package com.ecom.service;

import com.ecom.model.Category;

import java.util.List;

public interface CategoryService {
    public Category saveCategory(Category category);
    public boolean existCategory(String name);
    public List<Category> getAllCategory();

}
