package com.reviq.tenancy;

import com.reviq.security.EnableSecurityLib;
import com.reviq.shared.EnableSharedLib;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSharedLib
@EnableSecurityLib
public class ReviqTenancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviqTenancyApplication.class, args);
    }
}
