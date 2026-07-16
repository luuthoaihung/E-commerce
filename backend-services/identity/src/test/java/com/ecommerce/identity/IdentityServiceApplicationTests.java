package com.ecommerce.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootTest
@EnableScheduling
class IdentityServiceApplicationTests {

	public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
