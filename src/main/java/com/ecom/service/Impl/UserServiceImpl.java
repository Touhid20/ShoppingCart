package com.ecom.service.Impl;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepo;
import com.ecom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        String enCodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(enCodePassword);
        UserDtls saveUser = userRepo.save(user);
        return saveUser;
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public List<UserDtls> getAllUsers(String role) {
        return userRepo.findByRole(role);

    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDtls> findByUsers = userRepo.findById(id);
        if(findByUsers.isPresent()){
            UserDtls userDtls = findByUsers.get();
            userDtls.setIsEnable(status);
            userRepo.save(userDtls);
            return true;
        }
        return false;
    }
}
