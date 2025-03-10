package com.orbaic.email;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableEncryptableProperties
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class OrbaicEmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrbaicEmailApplication.class, args);
	}

}
