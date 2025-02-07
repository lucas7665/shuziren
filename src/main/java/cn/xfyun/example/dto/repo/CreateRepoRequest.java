package cn.xfyun.example.dto.repo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "创建知识库的请求参数")
public class CreateRepoRequest {
    @Schema(description = "知识库名称，必须唯一", example = "产品文档库")
    private String repoName;
    
    @Schema(description = "知识库描述，可选", example = "存放所有产品相关的文档")
    private String repoDesc;
    
    @Schema(description = "知识库标签，多个标签用逗号分隔", example = "产品,文档,API")
    private String repoTags;
} 