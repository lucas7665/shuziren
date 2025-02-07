package cn.xfyun.example.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.RepoChatRequest;
import cn.xfyun.example.util.ApiAuthUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class RepoChatService {

    @Value("${xfyun.appId}")
    private String appId;

    @Value("${xfyun.secret}")
    private String secret;

    @Value("${xfyun.chatUrl}")
    private String chatUrl;

    private final OkHttpClient client = new OkHttpClient();

    public ApiResponse<?> chat(RepoChatRequest request) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        // 构建WebSocket URL
        String wsUrl = chatUrl + "?appId=" + appId + "&timestamp=" + ts + "&signature=" + signature;
        
        // 构建请求体
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", request.getQuestion());

        JSONObject requestBody = new JSONObject();
        requestBody.set("repoId", request.getRepoId());
        requestBody.set("messages", JSONUtil.createArray().set(message));

        final StringBuilder buffer = new StringBuilder();
        final Object lock = new Object();

        WebSocket webSocket = client.newWebSocket(
            new Request.Builder().url(wsUrl).build(),
            new WebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    JSONObject response = JSONUtil.parseObj(text);
                    if (response.getInt("code") == 0) {
                        String content = response.getStr("content", "");
                        buffer.append(content);
                        
                        // 如果是最后一条消息，通知主线程
                        if (response.getInt("status") == 2) {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    } else {
                        log.error("问答失败: {}", text);
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.error("WebSocket连接失败", t);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        );

        // 发送问题
        webSocket.send(requestBody.toString());

        try {
            synchronized (lock) {
                lock.wait(30000); // 等待30秒
            }
        } catch (InterruptedException e) {
            log.error("等待回答超时", e);
            return ApiResponse.error("回答超时");
        } finally {
            webSocket.close(1000, "finish");
        }

        String answer = buffer.toString();
        return answer.isEmpty() ? 
            ApiResponse.error("未获取到回答") : 
            ApiResponse.success(answer);
    }
} 