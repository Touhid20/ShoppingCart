package com.ecom.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class CommonUtil {

    @Autowired
    private  JavaMailSender mailSender;

    public  Boolean sendMail(String url, String recipientMail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("javier08854@gmail.com","Shopping Cart");
        helper.setTo(recipientMail);

        String content = "<p>Hello,</p>" + "<p> You have request to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url + "\">Change my password</p>";

        helper.setSubject("Password Reset");
        helper.setText(content,true);
        mailSender.send(message);
        return true;
    }

    public static String generateUrl(HttpServletRequest request) {
       // http://localhost:8080/forgotPassword

        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(),"");
    }
}
