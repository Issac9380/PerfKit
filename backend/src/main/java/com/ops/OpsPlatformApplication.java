package com.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ops.mapper")
public class OpsPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpsPlatformApplication.class, args);
    }
}
