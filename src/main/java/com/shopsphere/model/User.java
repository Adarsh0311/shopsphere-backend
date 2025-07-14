package com.shopsphere.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID; // For UUID generation

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private String userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false) // Hashed password will be stored here
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Many-to-Many relationship with Role (a user can have many roles, a role can be assigned to many users)
    @ManyToMany(fetch = FetchType.EAGER) // Fetch roles eagerly as they're essential for authorization
    @JoinTable(
        name = "user_roles", // Name of the join table
        joinColumns = @JoinColumn(name = "user_id"), // Foreign key from 'users' table in join table
        inverseJoinColumns = @JoinColumn(name = "role_id") // Foreign key from 'roles' table in join table
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
    }

    // We won't set updatedAt for User entity unless specific user profile updates are managed
    // lastLogin will be managed separately


    public void addRole(Role role) {
        this.roles.add(role);
    }
}