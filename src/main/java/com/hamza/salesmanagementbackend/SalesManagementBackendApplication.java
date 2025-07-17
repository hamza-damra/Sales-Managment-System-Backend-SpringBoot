package com.hamza.salesmanagementbackend;

import com.hamza.salesmanagementbackend.config.CorsProperties;
import com.hamza.salesmanagementbackend.config.JwtConfig;
import com.hamza.salesmanagementbackend.config.UpdateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({
    JwtConfig.class,
    CorsProperties.class,
    UpdateProperties.class
})
public class SalesManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesManagementBackendApplication.class, args);
	}

}
