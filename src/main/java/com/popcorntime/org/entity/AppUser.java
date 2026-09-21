package com.popcorntime.org.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

// uk: unique key
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_email_phone", columnNames = { "email", "phone" })
})
public class AppUser {
    private String firstName;
    private String lastName;
    @Id
    private String email;
    private Long phone;
    @NotNull
    private String password;
    @ManyToOne
    private UserRole userRole;
}
