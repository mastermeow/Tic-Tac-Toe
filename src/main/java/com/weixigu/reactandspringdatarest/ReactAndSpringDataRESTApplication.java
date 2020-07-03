package com.weixigu.reactandspringdatarest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Application class of the React And Spring Data REST Application.
@SpringBootApplication
public class ReactAndSpringDataRESTApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

	public static void main(String[] args) {
		LOGGER.info("Initializing ReactAndSpringDataRESTApplication");
		org.springframework.boot.SpringApplication.run(ReactAndSpringDataRESTApplication.class, args);
	}

}
