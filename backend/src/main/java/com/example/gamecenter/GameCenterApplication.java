package com.example.gamecenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 游戏中心后端 Spring Boot 启动入口。 */
@SpringBootApplication
@MapperScan("com.example.gamecenter.mapper")
public class GameCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameCenterApplication.class, args);
    }

}