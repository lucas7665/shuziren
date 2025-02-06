package cn.xfyun.example.dto.chat;

import lombok.Data;
import lombok.Builder;



@Data
@Builder
public class ChatRequest {
    private String fileId;
    private String question;
} 