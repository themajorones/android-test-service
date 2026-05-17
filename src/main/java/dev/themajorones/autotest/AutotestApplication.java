package dev.themajorones.autotest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan("dev.themajorones.models.entity")
public class AutotestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutotestApplication.class, args);
	}

}
