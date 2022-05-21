package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(WebApplication.class);
		application.setDefaultProperties(Collections.singletonMap("server.port", 9090));
		application.run(args);
		//SpringApplication.run(WebApplication.class, args);
	}

}
