package cn.xfyun.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("讯飞文档问答 API")
                        .description("基于讯飞开放平台的文档问答系统API文档")
                        .version("1.0")
                        .contact(new Contact()
                                .name("开发者")
                                .url("https://www.xfyun.cn/")));
    }
} 