package com.solif.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
		io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class})
@EnableJpaAuditing  // JPA Auditing 활성화
@EnableAsync         // 비동기 이벤트 처리 활성화
public class BlueSolBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlueSolBackendApplication.class, args);
	}

}
