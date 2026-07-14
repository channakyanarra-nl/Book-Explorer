package com.nineleaps.BookExplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class BookExplorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookExplorerApplication.class, args);
	}

}
