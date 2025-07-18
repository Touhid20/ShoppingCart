package com.ecom.controller;

import com.ecom.model.*;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
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

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String home() {
        return "/user/home";
    }

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

    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
        Cart saveCart = cartService.saveCart(pid, uid);

        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("errorMsg", "Product add to cart Failed!!");
        } else {
            session.setAttribute("succMsg", "Product added to cart");
        }
        return "redirect:/product/" + pid;
    }

    @GetMapping("/cart")
    private String loadCartPage(Principal principal, Model model) {
        UserDtls user = getLoggedInUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            model.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "/user/cart";
    }

    private UserDtls getLoggedInUserDetails(Principal principal) {
        String email = principal.getName();
        UserDtls userDtls = userService.getUserByEmail(email);
        return userDtls;
    }

    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
        cartService.updateCartQuantity(sy, cid);
        return "redirect:/user/cart";
    }

    @GetMapping("/orders")
    public String cardPage(Principal principal, Model model) {
        UserDtls user = getLoggedInUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            double totalOrderPrice = orderPrice + 250 + 100;
            model.addAttribute("orderPrice", orderPrice);
            model.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "/user/order";
    }

    @PostMapping("/saveOrders")
    public String saveOrders(@ModelAttribute OrderRequest request, Principal principal) throws Exception {
//        System.out.println(request);
        UserDtls user = getLoggedInUserDetails(principal);
        orderService.saveOrder(user.getId(), request);
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String loadSuccess() {
        return "/user/success";
    }


    @GetMapping("/userOrder")
    public String userOrder(Model model, Principal principal) {
        UserDtls loginUser = getLoggedInUserDetails(principal);
        List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
        model.addAttribute("orders", orders);
        return "/user/userOrder";
    }

    @GetMapping("/updateStatus")
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
        return "redirect:/user/userOrder";
    }

    @GetMapping("/profile")
    public String profile() {
        return "/user/profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
        UserDtls updateUserProfile = userService.updateUserProfile(user, img);
        if (ObjectUtils.isEmpty(updateUserProfile)) {
            session.setAttribute("errorMsg", "profile not updated");
        } else {
            session.setAttribute("succMsg", "profile successfully updated");
        }
        return "redirect:/user/profile";
    }


    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal principal, HttpSession session) {
        UserDtls loggedInUserDetails = getLoggedInUserDetails(principal);
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
        if (matches) {
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);

            if (ObjectUtils.isEmpty(updateUser)){
                session.setAttribute("errorMsg", "Password not updated!! Something went wrong on server");
            }else {
                session.setAttribute("succMsg", "password updated successfully");
            }

        } else {
            session.setAttribute("errorMsg", "Current password id incorrect");
        }
        return "redirect:/user/profile";
    }

}
