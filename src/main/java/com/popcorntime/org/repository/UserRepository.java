package com.popcorntime.org.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.entity.AppUser;

@Repository
public interface UserRepository extends JpaRepository<AppUser, String>{
    Optional<AppUser> findByEmail(String email);
    
}
