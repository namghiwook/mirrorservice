package com.namghiwook.mirror;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "com.namghiwook.mirror" })
public class MirrorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MirrorApplication.class, args);
	}
}
