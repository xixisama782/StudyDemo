package com.example.gamecenter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/** 将本地头像目录映射为 /uploads/avatars/** 静态资源。 */
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    private final Path avatarDirectory;

    public WebResourceConfig(@Value("${app.upload.avatar-dir}") String avatarDir) {
        this.avatarDirectory = Paths.get(avatarDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = this.avatarDirectory.toUri().toString();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
