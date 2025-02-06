package cn.xfyun.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class FileStatusResp {
    private int code;
    private String sid;
    private String desc;
    private List<FileStatus> data;

    @Data
    public static class FileStatus {
        private String fileId;
        private String fileStatus;  // uploaded/texted/spliting/splited/vectoring/vectored/failed
    }
} 