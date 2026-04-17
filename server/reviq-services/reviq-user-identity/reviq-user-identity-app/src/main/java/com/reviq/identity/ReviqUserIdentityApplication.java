package com.reviq.identity;

import com.reviq.security.EnableSecurityLib;
import com.reviq.shared.EnableSharedLib;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSharedLib
@EnableSecurityLib
public class ReviqUserIdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviqUserIdentityApplication.class, args);
    }
}
