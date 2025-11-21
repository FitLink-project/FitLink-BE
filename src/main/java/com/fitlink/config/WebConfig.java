package com.fitlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

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
                .addResourceLocations(location)
                .setCachePeriod(3600); // 1시간 캐시 설정
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
        // Windows 경로를 절대 경로로 변환하고 file: 접두사 추가
        String absolutePath = Paths.get(path).toAbsolutePath().normalize().toString();
        String normalized = absolutePath.endsWith("/") || absolutePath.endsWith("\\") 
                ? absolutePath 
                : absolutePath + "/";
        // Windows 경로의 백슬래시를 슬래시로 변환
        normalized = normalized.replace("\\", "/");
        return "file:" + normalized;
    }
}

