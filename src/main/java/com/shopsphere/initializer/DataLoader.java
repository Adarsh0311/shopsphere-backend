package com.shopsphere.initializer;

import com.shopsphere.model.Role;
import com.shopsphere.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner { // to seed these roles automatically on application startup if they don't exist.
    private final RoleRepository roleRepository;

    /**
     * run method executed once, right after the Spring application context has been loaded and all beans have been created.
     * @param args method arguments
     */
    @Override
    public void run(String... args) {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
            System.out.println("ROLE_USER created.");
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
            System.out.println("ROLE_ADMIN created.");
        }
    }

}
