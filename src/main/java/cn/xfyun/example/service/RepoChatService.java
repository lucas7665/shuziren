package cn.xfyun.example.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;
import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.RepoChatRequest;
import cn.xfyun.example.util.ApiAuthUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


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

    private final RepoService repoService;

    @Autowired
    public RepoChatService(RepoService repoService) {
        this.repoService = repoService;
    }

    public ApiResponse<?> chat(RepoChatRequest request) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        // 构建WebSocket URL
        String wsUrl = chatUrl + "?appId=" + appId + "&timestamp=" + ts + "&signature=" + signature;
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        
        // 添加知识库ID或文件ID列表
        if (request.getRepoId() != null) {
            requestBody.set("repoId", request.getRepoId());
        }
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            requestBody.set("fileIds", request.getFileIds());
        }

        // 设置topN
        if (request.getTopN() != null) {
            requestBody.set("topN", request.getTopN());
        }

        // 构建消息列表
        JSONArray messages = JSONUtil.createArray();
        if (request.getMessages() != null) {
            for (RepoChatRequest.Message msg : request.getMessages()) {
                JSONObject msgObj = new JSONObject();
                msgObj.set("role", msg.getRole());
                msgObj.set("content", msg.getContent());
                messages.add(msgObj);
            }
        }
        // 添加当前问题
        JSONObject currentMsg = new JSONObject();
        currentMsg.set("role", "user");
        currentMsg.set("content", request.getQuestion());
        messages.add(currentMsg);
        requestBody.set("messages", messages);

        // 添加chatExtends配置
        if (request.getChatExtends() != null) {
            JSONObject chatExt = new JSONObject();
            RepoChatRequest.ChatExtends ext = request.getChatExtends();
            
            if (ext.getWikiPromptTpl() != null) {
                chatExt.set("wikiPromptTpl", ext.getWikiPromptTpl());
            }
            if (ext.getWikiFilterScore() != null) {
                chatExt.set("wikiFilterScore", ext.getWikiFilterScore());
                log.info("设置相似度阈值: {}", ext.getWikiFilterScore());
            }
            if (ext.getSpark() != null) {
                chatExt.set("spark", ext.getSpark());
                log.info("是否开启大模型兜底: {}", ext.getSpark());
            }
            if (ext.getTemperature() != null) {
                chatExt.set("temperature", ext.getTemperature());
            }
            
            requestBody.set("chatExtends", chatExt);
        }

        // 添加请求日志
        log.info("知识库问答请求: repoId={}, question={}, requestBody={}", 
            request.getRepoId(), request.getQuestion(), requestBody);

        final StringBuilder buffer = new StringBuilder();
        final JSONObject references = new JSONObject();
        final Object lock = new Object();

        WebSocket webSocket = client.newWebSocket(
            new Request.Builder().url(wsUrl).build(),
            new WebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    JSONObject response = JSONUtil.parseObj(text);
                    if (response.getInt("code") == 0) {
                        int status = response.getInt("status");
                        
                        if (status == 99) {
                            String fileRefer = response.getStr("fileRefer");
                            if (fileRefer != null) {
                                JSONObject fileRefs = JSONUtil.parseObj(fileRefer);
                                // 创建文件名列表，不包含chunks信息
                                JSONArray fileNames = JSONUtil.createArray();
                                fileRefs.forEach((fileId, chunks) -> {
                                    ApiResponse<?> fileResponse = repoService.getFileInfo(fileId);
                                    if (fileResponse.isSuccess() && fileResponse.getData() != null) {
                                        JSONObject fileInfo = JSONUtil.parseObj(fileResponse.getData());
                                        String fileName = fileInfo.getStr("fileName");
                                        if (fileName != null) {
                                            String cleanFileName = fileName.replaceAll("^[\\w-]+-", "")
                                                        .replaceAll("^\\d+", "");
                                            fileNames.add(cleanFileName);
                                        }
                                    }
                                });
                                // 设置文件名列表
                                references.set("files", fileNames);
                            }
                        } else {
                            String content = response.getStr("content", "");
                            buffer.append(content);
                            
                            if (status == 2) {
                                synchronized (lock) {
                                    lock.notify();
                                }
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
        if (answer.isEmpty()) {
            return ApiResponse.error("未获取到回答");
        }

        JSONObject result = new JSONObject();
        result.set("answer", answer);
        result.set("references", references);
        return ApiResponse.success(result);
    }
} 