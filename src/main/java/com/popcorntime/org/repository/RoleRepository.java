package com.popcorntime.org.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.dto.Role;
import com.popcorntime.org.entity.UserRole;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long>{
    Optional<UserRole> findByRole(Role role);
}
