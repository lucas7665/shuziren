package cn.xfyun.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 允许跨域的路径
            .allowedOrigins("http://localhost:5173")  // 允许的源
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
            .allowedHeaders("*")  // 允许的请求头
            .allowCredentials(true)  // 允许发送cookie
            .maxAge(3600);  // 预检请求的有效期，单位为秒
    }
} 