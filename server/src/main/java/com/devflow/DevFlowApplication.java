package com.devflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevFlow 应用启动类
 */
@SpringBootApplication
@MapperScan("com.devflow.mapper")
public class DevFlowApplication {

    /**
     * 应用入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DevFlowApplication.class, args);
    }
}
