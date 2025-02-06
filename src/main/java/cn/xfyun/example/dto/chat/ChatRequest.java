package cn.xfyun.example.dto.chat;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class ChatRequest {
    private String fileId;
    private String question;
} 