package com.xu.drools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@ComponentScan(basePackages ="com.xu.drools")
@EnableScheduling
@EnableSwagger2
@EnableRetry
public class DroolsSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(DroolsSpringbootApplication.class, args);
	}

}
