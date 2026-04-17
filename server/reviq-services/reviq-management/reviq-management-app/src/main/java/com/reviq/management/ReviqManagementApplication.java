package com.reviq.management;

import com.reviq.multitenancy.EnableMultitenancyLib;
import com.reviq.security.EnableSecurityLib;
import com.reviq.shared.EnableSharedLib;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSharedLib
@EnableSecurityLib
@EnableMultitenancyLib
public class ReviqManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviqManagementApplication.class, args);
    }
}
