package cn.xfyun.example.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xfyun.example.dto.UploadResp;
import cn.xfyun.example.dto.FileStatusResp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ChatDocUtil {
    public UploadResp upload(String filePath, String url, String appId, String secret) {
        File file = new File(filePath);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .addFormDataPart("fileType", "wiki")
                .addFormDataPart("parseType", "AUTO");

        long ts = System.currentTimeMillis() / 1000;
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", ApiAuthUtil.getSignature(appId, secret, ts))
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                return JSONUtil.toBean(response.body().string(), UploadResp.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public FileStatusResp getFileStatus(String fileStatusUrl, String fileId, String appId, String secret) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);
        
        // 构建form-data请求体
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileIds", fileId);  // 使用form-data格式
        
        log.info("状态查询请求参数: fileIds={}", fileId);
        
        Request request = new Request.Builder()
                .url(fileStatusUrl)
                .post(builder.build())
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();
                
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("状态查询响应: {}", responseBody);
            
            if (response.code() == 200) {
                return JSONUtil.toBean(responseBody, FileStatusResp.class);
            }
        } catch (IOException e) {
            log.error("获取文件状态失败", e);
        }
        return null;
    }

    public String chat(String chatUrl, String fileId, String question, String appId, String secret) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);
        
        final StringBuilder buffer = new StringBuilder();
        final Object lock = new Object();

        String wsUrl = chatUrl + "?appId=" + appId + "&timestamp=" + ts + "&signature=" + signature;
        log.info("开始连接WebSocket: {}", wsUrl);
        
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        WebSocket webSocket = okHttpClient.newWebSocket(
                new Request.Builder().url(wsUrl).build(),
                new WebSocketListener() {
                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        log.info("收到WebSocket消息: {}", text);
                        JSONObject jsonObject = JSONUtil.parseObj(text);
                        String content = jsonObject.getStr("content");
                        if (content != null) {
                            buffer.append(content);
                        }
                        if (jsonObject.getInt("status", 0) == 2) {
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

        JSONObject requestBody = new JSONObject();
        requestBody.set("fileId", fileId);
        requestBody.set("question", question);
        log.info("发送问题: {}", requestBody.toString());
        webSocket.send(requestBody.toString());

        try {
            synchronized (lock) {
                lock.wait(30000);
            }
        } catch (InterruptedException e) {
            log.error("等待回答超时", e);
        } finally {
            webSocket.close(1000, "finish");
        }

        return buffer.toString();
    }

    // ... 其他方法
} 