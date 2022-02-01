package com.asteroid.duck.pointy.query;

import com.asteroid.duck.pointy.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Provides a configuration for the resource handler to serve up the thumbnails folder(s)
 */
@Configuration
public class ImageHandlerConfig implements WebMvcConfigurer {
	@Autowired
	private Config config;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		final String resourceFolder = config.getDatabase().resolve("images").toAbsolutePath().toUri().toString();
		registry
						.addResourceHandler("/images/**/")
						.addResourceLocations(resourceFolder).resourceChain(false);
	}
}
