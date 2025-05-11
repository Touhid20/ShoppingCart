package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String index() {
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct() {
        return "admin/addProduct";
    }

    @GetMapping("/category")
    public String category(Model model) {
        model.addAttribute("categories", categoryService.getAllCategory());
        return "admin/category";
    }

//    @PostMapping("/saveCategory")
//    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session){
//        String imageName = file != null ? file.getOriginalFilename(): "default.jpg";
//        category.setImageName(imageName);
//
//        boolean existCategory = categoryService.existCategory(category.getName());
//        if(existCategory){
//            session.setAttribute("errorMsg","Category name already exists");
//        }else{
//            Category saveCategory = categoryService.saveCategory(category);
//            if(ObjectUtils.isEmpty(saveCategory)){
//                session.setAttribute("errorMsg","Not saved! internal server error");
//            }else {
//                session.setAttribute("succMsg","Saved successfully");
//            }
//        }
//        return "redirect:/admin/category";
//    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam("isActive") String isActiveStr,
                               HttpSession session) throws IOException {

        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        // Force boolean parsing
        category.setIsActive(Boolean.parseBoolean(isActiveStr));

        boolean existCategory = categoryService.existCategory(category.getName());
        if (existCategory) {
            session.setAttribute("errorMsg", "Category name already exists");
        } else {
            Category saveCategory = categoryService.saveCategory(category);
            if (ObjectUtils.isEmpty(saveCategory)) {
                session.setAttribute("errorMsg", "Not saved! internal server error");
            } else {
                File saveFile = new ClassPathResource("/static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                session.setAttribute("succMsg", "Saved successfully");
            }
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {

        boolean deleteCategory = categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("succMsg", "Category deleted Successfully");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again.");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/editCategory";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        Category cat = categoryService.getCategoryById(category.getId());
        String imageName = file.isEmpty() ? cat.getImageName() : file.getOriginalFilename();
        if (!ObjectUtils.isEmpty(category)) {
            cat.setName(category.getName());
            cat.setIsActive(category.getIsActive());
            cat.setImageName(imageName);
        }
        Category updateCategory = categoryService.saveCategory(cat);
        if (!ObjectUtils.isEmpty(updateCategory)) {
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("/static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            session.setAttribute("succMsg", "Category update successfully");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again.");
        }
        return "redirect:/admin/loadEditCategory/" + category.getId();
    }

}
