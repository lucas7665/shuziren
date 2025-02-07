package cn.xfyun.example.service;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.UploadResp;
import cn.xfyun.example.dto.chat.ChatRequest;
import cn.xfyun.example.util.ChatDocUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import cn.xfyun.example.dto.FileStatusResp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Slf4j
@Service
public class ChatService {

    @Value("${xfyun.appId}")
    private String appId;

    @Value("${xfyun.secret}")
    private String secret;

    @Value("${xfyun.uploadUrl}")
    private String uploadUrl;

    @Value("${xfyun.chatUrl}")
    private String chatUrl;

    @Value("${xfyun.fileStatusUrl}")
    private String fileStatusUrl;

    private final ChatDocUtil chatDocUtil = new ChatDocUtil();

    // 使用ConcurrentHashMap存储文件信息
    private final ConcurrentHashMap<String, FileInfo> fileMap = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    public static class FileInfo {
        private String fileId;
        private String fileName;
        private String status;
        private long uploadTime;
    }

    public ApiResponse<?> uploadFile(MultipartFile file) {
        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), file.getOriginalFilename());
            file.transferTo(tempFile);
            log.info("临时文件创建成功: {}", tempFile.getAbsolutePath());

            UploadResp resp = chatDocUtil.upload(tempFile.getAbsolutePath(), uploadUrl, appId, secret);
            tempFile.delete();
            
            if (resp != null && resp.getCode() == 0) {
                String fileId = resp.getData().getFileId();
                
                // 等待文件向量化完成
                int maxAttempts = 30; // 最多等待30次
                int attempts = 0;
                String status = "";
                
                while (attempts < maxAttempts) {
                    FileStatusResp statusResp = chatDocUtil.getFileStatus(fileStatusUrl, fileId, appId, secret);
                    if (statusResp != null && statusResp.getCode() == 0 && !statusResp.getData().isEmpty()) {
                        status = statusResp.getData().get(0).getFileStatus();
                        log.info("文件状态: {}", status);
                        
                        if ("vectored".equals(status)) {
                            // 向量化完成
                            fileMap.put(fileId, new FileInfo(
                                fileId,
                                file.getOriginalFilename(),
                                status,
                                System.currentTimeMillis()
                            ));
                            return ApiResponse.success(resp.getData());
                        } else if ("failed".equals(status)) {
                            return ApiResponse.error("文件处理失败");
                        }
                    }
                    
                    // 等待2秒后再次查询
                    Thread.sleep(2000);
                    attempts++;
                }
                
                // 超时但仍在处理中
                fileMap.put(fileId, new FileInfo(
                    fileId,
                    file.getOriginalFilename(),
                    status,
                    System.currentTimeMillis()
                ));
                return ApiResponse.success(resp.getData())
                    .setMessage("文件已上传，正在处理中，请稍后查看状态");
            } else {
                return ApiResponse.error(resp != null ? resp.getDesc() : "上传失败");
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> getFileStatus(String fileId) {
        try {
            FileStatusResp resp = chatDocUtil.getFileStatus(fileStatusUrl, fileId, appId, secret);
            if (resp != null && resp.getCode() == 0 && !resp.getData().isEmpty()) {
                // 更新文件状态
                FileInfo fileInfo = fileMap.get(fileId);
                if (fileInfo != null) {
                    fileInfo.setStatus(resp.getData().get(0).getFileStatus());
                }
                return ApiResponse.success(resp.getData().get(0).getFileStatus());
            } else {
                String errorMsg = resp != null ? resp.getDesc() : "获取状态失败";
                log.error("状态查询失败: {}", errorMsg);
                return ApiResponse.error(errorMsg);
            }
        } catch (Exception e) {
            log.error("获取文件状态失败", e);
            return ApiResponse.error("获取状态失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> getAllFiles() {
        return ApiResponse.success(fileMap.values().stream()
            .sorted((a, b) -> Long.compare(b.getUploadTime(), a.getUploadTime()))  // 按上传时间倒序
            .collect(Collectors.toList()));
    }

    public ApiResponse<?> chat(ChatRequest request) {
        try {
            String answer = chatDocUtil.chat(chatUrl, request.getFileId(), request.getQuestion(), appId, secret);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            log.error("问答失败", e);
            return ApiResponse.error("问答失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> getFileList() {
        return ApiResponse.success(fileMap.values().stream()
            .filter(file -> "vectored".equals(file.getStatus()))
            .collect(Collectors.toList()));
    }
} 