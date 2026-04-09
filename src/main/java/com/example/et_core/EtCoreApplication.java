package com.example.et_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class EtCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtCoreApplication.class, args);
	}

}
