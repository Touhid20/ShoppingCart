package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UserDtls {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String mobileNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String password;
    private String profileImage;
    private String role;
    private Boolean isEnable;
    private Boolean accountNonLocked;
    private Integer failedAttempt;
    private Date lockTime;
    private String resetToken;


}
