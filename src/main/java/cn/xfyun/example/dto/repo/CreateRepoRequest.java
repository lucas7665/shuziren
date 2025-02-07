package cn.xfyun.example.dto.repo;

import lombok.Data;

@Data
public class CreateRepoRequest {
    private String repoName;
    private String repoDesc;
    private String repoTags;
} 