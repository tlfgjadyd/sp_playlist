package com.playlist.myplaylist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.playlist.myplaylist.mapper")
public class MyplaylistApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyplaylistApplication.class, args);
    }

}
