package com.ecom.controller;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/home")
    public String home(){
        return "/user/home";
    }

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if ((principal != null)) {
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user",userDtls);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categories",allActiveCategory);

    }
    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session){
        Cart saveCart = cartService.saveCart(pid, uid);

        if(ObjectUtils.isEmpty(saveCart)){
            session.setAttribute("errorMsg","Product add to cart Failed!!");
        }else {
            session.setAttribute("succMsg","Product added to cart");
        }
        return "redirect:/product/" + pid;
    }
}
