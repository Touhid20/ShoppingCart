package com.ecom.controller;

import ch.qos.logback.core.util.StringUtil;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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
import java.util.UUID;

@Controller

public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

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
        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @GetMapping("/products")
    public String products(Model model, @RequestParam(value = "category", defaultValue = "")
                           String category, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "9") int pageSize, @RequestParam(defaultValue = "") String text) {

//        System.out.println("category="+category);
        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("paramValue", category);
        model.addAttribute("categories", categories);

//        List<Product> products = productService.getAllActiveProducts(category);
//        model.addAttribute("products", products);
        Page<Product> page = null;
        if (StringUtils.isEmpty(text)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        }else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category,text);
        }

        List<Product> products = page.getContent();
        model.addAttribute("products", products);
        model.addAttribute("productsSize", products.size());


        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "product";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model) {
        Product productById = productService.getProductById(id);
        model.addAttribute("product", productById);
        return "viewProduct";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
            throws IOException {

        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);
        UserDtls saveUser = userService.saveUser(user);

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

        return "redirect:/registration";
    }

    // Forgot Password
    @GetMapping("/forgotPassword")
    public String showForgotPassword() {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
            throws MessagingException, UnsupportedEncodingException {

        UserDtls userByEmail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid email");
        } else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);

            // Generate Url: http://localhost:8080/resetPassword?token=yyiuohsajhkfufauioahda
            String url = CommonUtil.generateUrl(request) + "/resetPassword?token=" + resetToken;

            Boolean sendMail = commonUtil.sendMail(url, email);
            if (sendMail) {
                session.setAttribute("succMsg", "mail has been sent. please check your email..");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server! email not send");
            }
        }
        return "redirect:/forgotPassword";
    }

    // Reset Password
    @GetMapping("/resetPassword")
    public String showResetPassword(@RequestParam String token, HttpSession session, Model model) {
        UserDtls userByToken = userService.getUserByToken(token);
        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("errorMsg", "Your link is invalid or expired!!");
            return "message";
        }
        model.addAttribute("token", token);
        return "resetPassword";
    }

    // Update Password
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session, Model model) {
        UserDtls userByToken = userService.getUserByToken(token);
        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("msg", "Your link is invalid or expired!!");
            return "message";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            // session.setAttribute("succMsg","Password change successfully");
            model.addAttribute("msg", "Password change successfully");
            return "message";
        }

    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String text, Model model) {
        List<Product> searchProducts = productService.searchProduct(text);
        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("products", searchProducts);
        model.addAttribute("categories", categories);
        return "/product";
    }


}
