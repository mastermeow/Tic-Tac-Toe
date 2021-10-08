package com.weixigu.boardgame;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Application class of the React And Spring Data REST Application.
@SpringBootApplication
public class BoardGameApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(BoardGameApplication.class);

	public static void main(String[] args) {
		LOGGER.info("Initializing ReactAndSpringDataRESTApplication");
		org.springframework.boot.SpringApplication.run(BoardGameApplication.class, args);
	}

}
