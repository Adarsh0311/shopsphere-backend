package com.shopsphere.initializer;

import com.shopsphere.model.Role;
import com.shopsphere.model.User;
import com.shopsphere.repository.RoleRepository;
import com.shopsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner { // to seed these roles automatically on application startup if they don't exist.
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * run method executed once, right after the Spring application context has been loaded and all beans have been created.
     * @param args method arguments
     */
    @Override
    public void run(String... args) {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
            log.info("ROLE_USER created.");
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
            log.info("ROLE_ADMIN created.");
        }

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@shopsphere.com");
            admin.setFirstName("Admin");

            roleRepository.findByName("ROLE_ADMIN").ifPresent(admin::addRole);
            userRepository.save(admin);
            log.info("Admin user created: {}", admin);
        }
    }

}
