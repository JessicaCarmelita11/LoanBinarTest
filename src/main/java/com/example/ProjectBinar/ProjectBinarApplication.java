package com.example.ProjectBinar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProjectBinarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectBinarApplication.class, args);
	}

}
