package cn.xfyun.example.dto.repo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "知识库问答请求参数")
public class RepoChatRequest {
    @Schema(description = "知识库ID", required = true)
    private String repoId;

    @Schema(description = "文件ID列表，最大200个", required = true)
    private List<String> fileIds;

    @Schema(description = "向量库文本段查询数量")
    private Integer topN;

    @Schema(description = "问题内容", required = true)
    private String question;

    @Schema(description = "历史对话记录")
    private List<Message> messages;

    @Schema(description = "大模型对话扩展配置")
    private ChatExtends chatExtends;

    @Data
    public static class Message {
        @Schema(description = "角色(user/assistant)")
        private String role;
        
        @Schema(description = "对话内容")
        private String content;
    }

    @Data
    public static class ChatExtends {
        @Schema(description = "wiki大模型问答模板")
        private String wikiPromptTpl;

        @Schema(description = "wiki结果分数阈值(0-1)", example = "0.82")
        private Float wikiFilterScore;

        @Schema(description = "未匹配到文档内容时是否使用大模型兜底")
        private Boolean spark;

        @Schema(description = "大模型问答温度(0-1)", example = "0.5")
        private Float temperature;
    }
} 