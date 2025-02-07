package cn.xfyun.example.dto.repo;

import lombok.Data;

@Data
public class RepoChatRequest {
    private String repoId;
    private String question;
} 