package cn.xfyun.example.dto.repo;

import lombok.Data;

@Data
public class RepoResponse {
    private String repoId;
    private String repoName;
    private String repoDesc;
    private String repoTags;
    private String createTime;
} 