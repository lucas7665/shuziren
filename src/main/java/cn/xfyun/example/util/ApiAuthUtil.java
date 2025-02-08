package cn.xfyun.example.util;

import cn.hutool.core.codec.Base64;
import cn.xfyun.example.dto.ApiResponse;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class ApiAuthUtil {
    public static String getSignature(String appId, String secret, long ts) {
        String auth = md5(appId + ts);
        return hmacSHA1(auth, secret);
    }

    private static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String hmacSHA1(String data, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static ApiResponse<?> get(String url, String appId, String secret) {
        long ts = System.currentTimeMillis() / 1000;
        String signature = getSignature(appId, secret, ts);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JSONObject result = JSONUtil.parseObj(responseBody);
            
            if (result.getInt("code") == 0) {
                return ApiResponse.success(result.get("data"));
            } else {
                return ApiResponse.error(result.getStr("desc"));
            }
        } catch (IOException e) {
            return ApiResponse.error("请求失败: " + e.getMessage());
        }
    }
} 