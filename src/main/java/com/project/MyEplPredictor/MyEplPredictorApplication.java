package com.project.MyEplPredictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // allows @Scheduled methods in services
public class MyEplPredictorApplication {

	public static void main(String[] args) {

		SpringApplication.run(MyEplPredictorApplication.class, args);
	}

}
