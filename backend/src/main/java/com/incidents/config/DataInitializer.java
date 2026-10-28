package com.incidents.config;

import com.incidents.model.Role;
import com.incidents.model.User;
import com.incidents.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Comptes de test (ADMIN et TECHNICIAN ne peuvent pas etre crees par /auth/register). */
@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UserRepository users, PasswordEncoder enc) {
        return args -> {
            if (users.count() > 0) return;
            create(users, enc, "Admin Principal", "admin@test.com", "Admin123!", Role.ADMIN);
            create(users, enc, "Technicien A", "tech@test.com", "Tech1234!", Role.TECHNICIAN);
            create(users, enc, "Technicien B", "tech2@test.com", "Tech1234!", Role.TECHNICIAN);
            create(users, enc, "Ahmed Benali", "emp1@test.com", "Employee1!", Role.EMPLOYEE);
            create(users, enc, "Sara Alaoui", "emp2@test.com", "Employee1!", Role.EMPLOYEE);
        };
    }
    private void create(UserRepository r, PasswordEncoder enc, String name, String email, String pwd, Role role) {
        User u = new User();
        u.setFullName(name); u.setEmail(email); u.setPassword(enc.encode(pwd)); u.setRole(role);
        r.save(u);
    }
}
