package com.MingYi.kill.server;


import com.MingYi.kill.api.Main;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(value = {"classpath:spring/spring-jdbc.xml"})
@MapperScan(basePackages = "com.MingYi.kill.model.mapper")
public class MainApplication extends SpringBootServletInitializer {

    //可以自己指定配置文件
    //不一定是默认的配置文件 application.properties 和 application.yml
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
        return builder.sources(MainApplication.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
