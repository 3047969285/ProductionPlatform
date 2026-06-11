package com.devflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.devflow.mapper")
public class DevFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevFlowApplication.class, args);
    }
}
