package com.asteroid.duck.pointy.indexer.checksum;

import com.asteroid.duck.pointy.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
	@Test
	public void testJsonRoundTrip() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Config config = Config.withDefaults()
						//.database(Paths.get("test-database").toAbsolutePath())
						.scanRoots(Set.of(Paths.get("c:\\a\\b\\c"), Paths.get("c:\\d\\e\\f")))
						.build();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
		System.out.println(json);

		Config reconstitutedConfig = mapper.readValue(json, Config.class);
		assertNotNull(reconstitutedConfig);
		assertEquals(config, reconstitutedConfig);
	}

}
