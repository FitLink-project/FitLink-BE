package com.fitlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.dir}")
    private String uploadPath;

    @Value("${file.url}")
    private String resourcePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String handlerPattern = ensureEndsWithWildcard(resourcePath);
        String location = ensureDirectoryPrefix(uploadPath);

        registry.addResourceHandler(handlerPattern)
                .addResourceLocations(location);
    }

    private String ensureEndsWithWildcard(String path) {
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("file.url property must not be empty");
        }
        return path.endsWith("**") ? path : (path.endsWith("/") ? path + "**" : path + "/**");
    }

    private String ensureDirectoryPrefix(String path) {
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("file.dir property must not be empty");
        }
        String normalized = path.endsWith("/") ? path : path + "/";
        return "file:" + normalized;
    }
}

