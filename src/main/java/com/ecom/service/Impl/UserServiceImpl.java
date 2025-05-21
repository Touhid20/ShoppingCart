package com.ecom.service.Impl;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepo;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
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
        if (findByUsers.isPresent()) {
            UserDtls userDtls = findByUsers.get();
            userDtls.setIsEnable(status);
            userRepo.save(userDtls);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepo.save(user);
    }

    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepo.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(UserDtls user) {
        long lockTime = user.getLockTime().getTime();
        long unLockTime = lockTime+ AppConstant.UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();

        if(unLockTime<currentTime){
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        UserDtls findByEmail = userRepo.findByEmail(email);
        findByEmail.setResetToken(resetToken);
        userRepo.save(findByEmail);
    }

    @Override
    public UserDtls getUserByToken(String token) {
        return userRepo.findByResetToken(token);

    }

    @Override
    public UserDtls updateUser(UserDtls user) {
       return userRepo.save(user);
    }
}
