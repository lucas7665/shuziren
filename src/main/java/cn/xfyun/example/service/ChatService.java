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

import cn.xfyun.example.dto.FileStatusResp;

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

    public ApiResponse<?> uploadFile(MultipartFile file) {
        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), file.getOriginalFilename());
            file.transferTo(tempFile);
            log.info("临时文件创建成功: {}", tempFile.getAbsolutePath());

            UploadResp resp = chatDocUtil.upload(tempFile.getAbsolutePath(), uploadUrl, appId, secret);
            tempFile.delete();

            if (resp != null && resp.getCode() == 0) {
                return ApiResponse.success(resp.getData());
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

    public ApiResponse<?> chat(ChatRequest request) {
        try {
            String answer = chatDocUtil.chat(chatUrl, request.getFileId(), request.getQuestion(), appId, secret);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            log.error("问答失败", e);
            return ApiResponse.error("问答失败: " + e.getMessage());
        }
    }
} 