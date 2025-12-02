package com.playlist.myplaylist.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PostgreSQL DB 연결을 위한 빈 설정 및 구성
 * datasource 를 객체로 생성
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary // 우선 사용 객체
    @ConfigurationProperties("spring.datasource") //yml -> spring.datasource
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl(); // datasource.url
        String username = properties.getUsername(); // datasource.username
        String password = properties.getPassword(); // datasource.password

        Pattern pattern = Pattern.compile("jdbc:postgresql://([^:]+):([^@]+)@(.+)"); // 실제 url형태
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) { // 일치하면
            username = matcher.group(1);
            password = matcher.group(2);
            url = "jdbc:postgresql://" + matcher.group(3); // jdbc:postgresql://<host>:<port>/<db_name> 이런식으로 url에서 사용자 정보 내용 제거
        }

        return DataSourceBuilder.create() // datasource 객체 생성
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(properties.getDriverClassName())
                .build();
    }
}
