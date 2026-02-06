package com.solif.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
		io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class})
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class BlueSolBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlueSolBackendApplication.class, args);
	}

}
