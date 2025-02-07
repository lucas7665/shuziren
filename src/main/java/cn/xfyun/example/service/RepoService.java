package cn.xfyun.example.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.CreateRepoRequest;
import cn.xfyun.example.util.ApiAuthUtil;
import cn.xfyun.example.util.ChatDocUtil;
import cn.xfyun.example.dto.FileStatusResp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RepoService {

    @Value("${xfyun.appId}")
    private String appId;

    @Value("${xfyun.secret}")
    private String secret;

    @Value("${xfyun.domain}")
    private String domain;

    private final OkHttpClient client = new OkHttpClient();
    private final ChatDocUtil chatDocUtil = new ChatDocUtil();

    public ApiResponse<?> createRepo(CreateRepoRequest request) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        JSONObject requestBody = new JSONObject();
        requestBody.set("repoName", request.getRepoName());
        requestBody.set("repoDesc", request.getRepoDesc());
        requestBody.set("repoTags", request.getRepoTags());

        Request httpRequest = new Request.Builder()
                .url(domain + "/openapi/v1/repo/create")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(httpRequest).execute();
            String responseBody = response.body().string();
            log.info("创建知识库响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                return ApiResponse.success(result.getStr("data"));
            } else {
                return ApiResponse.error(result.getStr("desc"));
            }
        } catch (IOException e) {
            log.error("创建知识库失败", e);
            return ApiResponse.error("创建知识库失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> getRepoList() {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        JSONObject requestBody = new JSONObject();
        requestBody.set("currentPage", 1);
        requestBody.set("pageSize", 100);

        Request request = new Request.Builder()
                .url(domain + "/openapi/v1/repo/list")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("获取知识库列表响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                JSONObject data = result.getJSONObject("data");
                if (data != null) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (Object item : data.getJSONArray("rows")) {
                        JSONObject obj = (JSONObject) item;
                        Map<String, Object> row = new HashMap<>();
                        row.put("repoId", obj.getStr("repoId"));
                        row.put("repoName", obj.getStr("repoName"));
                        row.put("repoDesc", obj.getStr("repoDesc"));
                        row.put("repoTags", obj.getStr("repoTags"));
                        row.put("createTime", obj.getStr("createTime"));
                        rows.add(row);
                    }
                    return ApiResponse.success(rows);
                }
            }
            return ApiResponse.error(result.getStr("desc"));
        } catch (IOException e) {
            log.error("获取知识库列表失败", e);
            return ApiResponse.error("获取知识库列表失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> getRepoFiles(String repoId) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        JSONObject requestBody = new JSONObject();
        requestBody.set("repoId", repoId);
        requestBody.set("currentPage", 1);
        requestBody.set("pageSize", 100);

        Request request = new Request.Builder()
                .url(domain + "/openapi/v1/repo/file/list")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("获取知识库文件列表响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                JSONObject data = result.getJSONObject("data");
                if (data != null) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (Object item : data.getJSONArray("rows")) {
                        JSONObject obj = (JSONObject) item;
                        Map<String, Object> row = new HashMap<>();
                        row.put("fileId", obj.getStr("fileId"));
                        row.put("fileName", obj.getStr("fileName"));
                        row.put("fileStatus", obj.getStr("fileStatus"));
                        row.put("createTime", obj.getStr("createTime"));
                        rows.add(row);
                    }
                    return ApiResponse.success(rows);
                }
            }
            return ApiResponse.error(result.getStr("desc"));
        } catch (IOException e) {
            log.error("获取知识库文件列表失败", e);
            return ApiResponse.error("获取知识库文件列表失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> deleteRepo(String repoId) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("repoId", repoId);

        Request request = new Request.Builder()
                .url(domain + "/openapi/v1/repo/del")
                .post(builder.build())
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("删除知识库响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(result.getStr("desc"));
            }
        } catch (IOException e) {
            log.error("删除知识库失败", e);
            return ApiResponse.error("删除知识库失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> removeFile(String repoId, String fileId) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);

        JSONObject requestBody = new JSONObject();
        requestBody.set("repoId", repoId);
        requestBody.set("fileIds", JSONUtil.createArray().set(fileId));

        Request request = new Request.Builder()
                .url(domain + "/openapi/v1/repo/file/remove")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("移除文件响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(result.getStr("desc"));
            }
        } catch (IOException e) {
            log.error("移除文件失败", e);
            return ApiResponse.error("移除文件失败: " + e.getMessage());
        }
    }

    public ApiResponse<?> addFileToRepo(String repoId, String fileId) {
        // 先检查文件状态
        try {
            FileStatusResp statusResp = chatDocUtil.getFileStatus(domain + "/openapi/v1/file/status", fileId, appId, secret);
            if (statusResp != null && statusResp.getCode() == 0 && !statusResp.getData().isEmpty()) {
                String status = statusResp.getData().get(0).getFileStatus();
                if (!"vectored".equals(status)) {
                    return ApiResponse.error("文件还未完成向量化，当前状态: " + status + "，请等待向量化完成后再添加");
                }
            }

            // 文件已向量化，执行添加操作
            long ts = System.currentTimeMillis() / 1000;
            String signature = ApiAuthUtil.getSignature(appId, secret, ts);

            JSONObject requestBody = new JSONObject();
            requestBody.set("repoId", repoId);
            requestBody.set("fileIds", JSONUtil.createArray().set(fileId));

            Request request = new Request.Builder()
                    .url(domain + "/openapi/v1/repo/file/add")
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .addHeader("appId", appId)
                    .addHeader("timestamp", String.valueOf(ts))
                    .addHeader("signature", signature)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("添加文件到知识库响应: {}", responseBody);
            
            JSONObject result = JSONUtil.parseObj(responseBody);
            if (result.getInt("code") == 0) {
                JSONObject data = result.getJSONObject("data");
                if (data != null && data.getJSONArray("failedList").isEmpty()) {
                    return ApiResponse.success(null);
                } else {
                    return ApiResponse.error("添加失败，请确认文件状态");
                }
            } else {
                return ApiResponse.error(result.getStr("desc"));
            }
        } catch (IOException e) {
            log.error("添加文件到知识库失败", e);
            return ApiResponse.error("添加文件到知识库失败: " + e.getMessage());
        }
    }

    // ... 其他方法实现
} 