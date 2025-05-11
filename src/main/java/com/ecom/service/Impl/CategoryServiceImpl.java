package com.ecom.service.Impl;
import com.ecom.model.Category;
import com.ecom.repository.CategoryRepo;
import com.ecom.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public Category saveCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Override
    public boolean existCategory(String name) {
        return categoryRepo.existsByName(name);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }
}
