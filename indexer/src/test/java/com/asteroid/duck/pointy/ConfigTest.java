package com.asteroid.duck.pointy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
	@Test
	public void testJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Config config = Config.withDefaults().database(Paths.get("test-database")).build();
		System.out.println(mapper.writeValueAsString(config));
	}

}