package net.playercounts.apigateway.config;

import net.playercounts.apigateway.entity.Account;
import net.playercounts.apigateway.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String adminUsername = "admin";

        boolean exists = userRepository.findByUsername(adminUsername).isPresent();

        if (exists) {
            return;
        }

        Account admin = new Account();

        admin.setUsername(adminUsername);

        admin.setPassword(
                passwordEncoder.encode("password")
        );

        admin.setRole("ROLE_ADMIN");

        userRepository.save(admin);

        System.out.println("ADMIN ACCOUNT CREATED");
    }
}