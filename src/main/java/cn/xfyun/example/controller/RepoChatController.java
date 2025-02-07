package cn.xfyun.example.controller;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.RepoChatRequest;
import cn.xfyun.example.service.RepoChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/repo-chat")
@RequiredArgsConstructor
public class RepoChatController {
    
    private final RepoChatService repoChatService;

    @PostMapping
    public ApiResponse<?> chat(@RequestBody RepoChatRequest request) {
        log.info("知识库问答请求: {}", request);
        return repoChatService.chat(request);
    }
} 