package com.vitaldrop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VitaldropApplication {

	public static void main(String[] args) {
		SpringApplication.run(VitaldropApplication.class, args);
	}

}
