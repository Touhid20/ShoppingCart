package com.ecom.config;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepo;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");
        UserDtls userDtls = userRepo.findByEmail(email);
        if (!ObjectUtils.isEmpty(userDtls)) {
            if (userDtls.getIsEnable()) {
                if (userDtls.getAccountNonLocked()) {
                    if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
                        userService.increaseFailedAttempt(userDtls);
                    } else {
                        userService.userAccountLock(userDtls);
                        exception = new LockedException("The account is Locked!! Failed attempt 3");
                    }

                } else {
                    if (userService.unlockAccountTimeExpired(userDtls)) {
                        exception = new LockedException("The account is Unlocked!! Please try to login");
                    } else {
                        exception = new LockedException("The account is locked !! Please try letter");
                    }

                }

            } else {
                exception = new LockedException("The account is inactive");
            }
        }else {
            exception = new LockedException("Invalid user name & password");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }


}
