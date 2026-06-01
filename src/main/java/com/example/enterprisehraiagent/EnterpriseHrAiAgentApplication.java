package com.example.enterprisehraiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.enterprisehraiagent.mapper")
@SpringBootApplication
public class EnterpriseHrAiAgentApplication {

    /**
     * 启动 Spring Boot 应用。
     */
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseHrAiAgentApplication.class, args);
    }
}
