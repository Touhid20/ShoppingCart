package com.ecom.repository;

import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserDtls,Integer> {
}
