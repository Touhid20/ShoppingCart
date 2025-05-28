package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.*;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
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
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;


    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if ((principal != null)) {
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart",countCart);

        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categories", allActiveCategory);

    }

    @GetMapping("/")
    public String index() {
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model) {
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);
        return "admin/addProduct";
    }

    @GetMapping("/category")
    public String category(Model model) {
        model.addAttribute("categories", categoryService.getAllCategory());
        return "admin/category";
    }


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


    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
                              HttpSession session) throws IOException {
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImageName(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        productService.saveProduct(product);


        if (!ObjectUtils.isEmpty(product)) {
            File saveFile = new ClassPathResource("/static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());

            System.out.println(path);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            session.setAttribute("succMsg", "Product saved Successfully");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again.");
        }
        return "redirect:/admin/loadAddProduct";
    }

//    @PostMapping("/saveProduct")
//    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
//                              HttpSession session) throws IOException {
//        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
//        product.setImageName(imageName);
//
//        productService.saveProduct(product);
//
//        if (!ObjectUtils.isEmpty(product)) {
//            // ✅ Path to src/main/resources/static/img/product_img
//            String projectPath = System.getProperty("user.dir"); // D:\Self Development\shoppingCart
//            String staticImagePath = projectPath + "/src/main/resources/static/img/product_img";
//
//            File dir = new File(staticImagePath);
//            if (!dir.exists()) dir.mkdirs(); // ফোল্ডার না থাকলে তৈরি করবে
//
//            Path path = Paths.get(staticImagePath + File.separator + image.getOriginalFilename());
//            System.out.println("Saving to: " + path);
//
//            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//            session.setAttribute("succMsg", "Product saved Successfully");
//        } else {
//            session.setAttribute("errorMsg", "Something Wrong! Try again.");
//        }
//
//        return "redirect:/admin/loadAddProduct";
//    }


    @GetMapping("/products")
    public String loadViewProduct(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        Boolean deleteProduct = productService.deleteProduct(id);
        if (deleteProduct) {
            session.setAttribute("succMsg", "Product delete Successfully");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again.");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("categories", categoryService.getAllCategory());
        return "/admin/editProduct";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file")
    MultipartFile image, HttpSession session, Model model) {
        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "Invalid Discount.");
        } else {
            Product updateProduct = productService.updateProduct(product, image);
            if (!ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("succMsg", "Product update Successfully");
            } else {
                session.setAttribute("errorMsg", "Something Wrong! Try again");
            }
        }
        return "redirect:/admin/editProduct/" + product.getId();

    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<UserDtls> users = userService.getAllUsers("ROLE_USER");
        model.addAttribute("users", users);
        return "/admin/users";
    }

    @GetMapping("/updateStatus")
    public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
        Boolean f = userService.updateAccountStatus(id, status);
        if(f){
            session.setAttribute("succMsg","Account status updated");
        }else {
            session.setAttribute("errorMsg","Something Wrong! Try again");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model) {
        List<ProductOrder> orders = orderService.getAllOrders();
        model.addAttribute("orders",orders);
        return "/admin/orders";
    }

    @PostMapping("/updateOrderStatus")
    public String updateStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

        OrderStatus[] values = OrderStatus.values();
        String status = null;
        System.out.println("Values: " + values);
        for (OrderStatus orderStatus : values) {
            if (orderStatus.getId().equals(st)) {
                status = orderStatus.getName();
            }
        }
        ProductOrder updateOrder = orderService.updateStatus(id, status);

        try {
            commonUtil.sendMailForProductOrder(updateOrder,status);
        } catch (Exception e) {
           e.printStackTrace();
        }
        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.setAttribute("succMsg", "Status updated successfully");
        } else {
            session.setAttribute("errorMsg", "Status not updated");
        }
        return "redirect:/admin/orders";
    }
}
