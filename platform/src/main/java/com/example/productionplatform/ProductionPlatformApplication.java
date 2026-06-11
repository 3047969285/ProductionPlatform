package com.example.productionplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.productionplatform")
@MapperScan("com.example.productionplatform.admin.mapper")
public class ProductionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductionPlatformApplication.class, args);
    }
}
