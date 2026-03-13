package com.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IM 通信系统启动类
 */
@SpringBootApplication
@MapperScan("com.im.mapper")
@EnableAsync
@EnableScheduling
public class ImApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
        System.out.println("============================================");
        System.out.println("    IM Communication System Started!        ");
        System.out.println("    Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("============================================");
    }
}
