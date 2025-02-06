package cn.xfyun.example.dto;

import lombok.Data;

@Data
public class UploadResp {
    private boolean flag;
    private String sid;
    private int code;
    private String desc;
    private DataInfo data;

    @Data
    public static class DataInfo {
        private String quantity;
        private String parseType;
        private String fileId;
    }
} 