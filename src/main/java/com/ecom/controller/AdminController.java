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
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;


    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if ((principal != null)) {
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);

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
    public String category(Model model, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "2") int pageSize) {
//        model.addAttribute("categories", categoryService.getAllCategory());
        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
        List<Category> categories = page.getContent();
        model.addAttribute("categories", categories);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
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
    public String loadViewProduct(Model model, @RequestParam(defaultValue = "") String text, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                                  @RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {
//        List<Product> products = null;
//        if (!ObjectUtils.isEmpty(text)) {
//            products = productService.searchProduct(text);
//        } else {
//            products = productService.getAllProducts();
//        }
//        model.addAttribute("products", products);

        Page<Product> page = null;
        if (!ObjectUtils.isEmpty(text)) {
            page = productService.searchProductPagination(pageNo, pageSize, text);
        } else {
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }
        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

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
    public String getAllUsers(Model model, @RequestParam int type) {
        List<UserDtls> users = null;
        if (type == 1) {
            users = userService.getAllUsers("ROLE_USER");
        }else {
            users = userService.getAllUsers("ROLE_ADMIN");
        }

        model.addAttribute("userType",type);
        model.addAttribute("users", users);
        return "/admin/users";
    }

    @GetMapping("/updateStatus")
    public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
        Boolean f = userService.updateAccountStatus(id, status);
        if (f) {
            session.setAttribute("succMsg", "Account status updated");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again");
        }
        return "redirect:/admin/users?type="+type;
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model, @RequestParam(defaultValue = "") String text, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "4") int pageSize) {
//        List<ProductOrder> orders = orderService.getAllOrders();
//        model.addAttribute("orders", orders);
//        model.addAttribute("search", false);

        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("search", false);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

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
            commonUtil.sendMailForProductOrder(updateOrder, status);
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

    @GetMapping("/searchOrder")
    public String searchProduct(@RequestParam String orderId, Model model, HttpSession session, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "4") int pageSize) {
        if (ObjectUtils.isEmpty(orderId)) {
//            List<ProductOrder> orders = orderService.getAllOrders();
//            model.addAttribute("orders", orders);
//            model.addAttribute("search", false);

            Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
            model.addAttribute("orders", page);
            model.addAttribute("search", false);

            model.addAttribute("pageNo", page.getNumber());
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalElements", page.getTotalElements());
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("isFirst", page.isFirst());
            model.addAttribute("isLast", page.isLast());

        } else {
            ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
            if (ObjectUtils.isEmpty(order)) {
                session.setAttribute("errorMsg", "Incorrect Order Id");
                model.addAttribute("order", null);
            } else {
                model.addAttribute("order", order);
            }
            model.addAttribute("search", true);

            model.addAttribute("pageNo", 0);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalElements", 1);
            model.addAttribute("isFirst", true);
            model.addAttribute("isLast", true);
        }

        return "/admin/orders";
    }

    @GetMapping("/addAdmin")
    public String addAdmin() {
        return "/admin/addAdmin";
    }

    @PostMapping("/saveAdmin")
    public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
            throws IOException {

        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);
        UserDtls saveUser = userService.saveAdmin(user);

        if (!ObjectUtils.isEmpty(saveUser)) {
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
                        + file.getOriginalFilename());

                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            session.setAttribute("succMsg", "User added successfully");
        } else {
            session.setAttribute("errorMsg", "Something Wrong! Try again.");
        }

        return "redirect:/admin/addAdmin";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
        UserDtls updateUserProfile = userService.updateUserProfile(user, img);
        if (ObjectUtils.isEmpty(updateUserProfile)) {
            session.setAttribute("errorMsg", "profile not updated");
        } else {
            session.setAttribute("succMsg", "profile successfully updated");
        }
        return "redirect:/admin/profile";
    }

    @GetMapping("/profile")
    public String profile() {
        return "/admin/profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal principal, HttpSession session) {
        UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(principal);
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
        if (matches) {
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);

            if (ObjectUtils.isEmpty(updateUser)) {
                session.setAttribute("errorMsg", "Password not updated!! Something went wrong on server");
            } else {
                session.setAttribute("succMsg", "password updated successfully");
            }

        } else {
            session.setAttribute("errorMsg", "Current password id incorrect");
        }
        return "redirect:/admin/profile";
    }


}
