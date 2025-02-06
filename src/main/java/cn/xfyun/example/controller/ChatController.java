package cn.xfyun.example.controller;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.chat.ChatRequest;
import cn.xfyun.example.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;

    @PostMapping("/upload")
    public ApiResponse<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("接收到文件上传请求: {}", file.getOriginalFilename());
        return chatService.uploadFile(file);
    }

    @GetMapping("/file/status/{fileId}")
    public ApiResponse<?> getFileStatus(@PathVariable String fileId) {
        log.info("查询文件状态: {}", fileId);
        return chatService.getFileStatus(fileId);
    }

    @PostMapping("/chat")
    public ApiResponse<?> chat(@RequestBody ChatRequest request) {
        log.info("接收到问答请求: {}", request);
        return chatService.chat(request);
    }

    @GetMapping("/files")
    public ApiResponse<?> getFileList() {
        log.info("获取文件列表");
        return chatService.getFileList();
    }
} 