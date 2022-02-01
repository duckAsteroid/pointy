package com.asteroid.duck.pointy.query;

import com.asteroid.duck.pointy.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

@SpringBootApplication
public class App {


	@Bean
	public static Config loadConfiguration(@Value("${pointy.db}") Path databaseDir) throws IOException {
		return Config.readFrom(databaseDir, Collections.emptySet());
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
