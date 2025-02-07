package cn.xfyun.example.controller;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.RepoChatRequest;
import cn.xfyun.example.service.RepoChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识库问答", description = "基于知识库的智能问答接口")
@RestController
@RequestMapping("/api/repo-chat")
@RequiredArgsConstructor
public class RepoChatController {
    
    private final RepoChatService repoChatService;

    @Operation(summary = "知识库问答",
        description = "基于知识库进行智能问答，支持以下高级特性：\n" +
            "1. 多轮对话：通过messages参数传递历史对话\n" +
            "2. 文本匹配：通过wikiFilterScore控制匹配精度\n" +
            "3. 大模型调优：通过temperature控制回答随机性\n" +
            "4. 自定义模板：通过wikiPromptTpl自定义问答模板\n" +
            "5. 兜底回答：通过spark参数开启大模型兜底\n\n" +
            "示例请求：\n" +
            "```json\n" +
            "{\n" +
            "  \"repoId\": \"xxx\",\n" +
            "  \"question\": \"如何使用API?\",\n" +
            "  \"topN\": 3,\n" +
            "  \"messages\": [\n" +
            "    {\"role\": \"user\", \"content\": \"之前的问题\"},\n" +
            "    {\"role\": \"assistant\", \"content\": \"之前的回答\"}\n" +
            "  ],\n" +
            "  \"chatExtends\": {\n" +
            "    \"wikiPromptTpl\": \"基于以下内容回答问题：\\n<wikicontent>\\n\\n问题：<wikiquestion>\\n回答：\",\n" +
            "    \"wikiFilterScore\": 0.82,\n" +
            "    \"spark\": true,\n" +
            "    \"temperature\": 0.5\n" +
            "  }\n" +
            "}\n" +
            "```")
    @PostMapping
    public ApiResponse<?> chat(@RequestBody RepoChatRequest request) {
        return repoChatService.chat(request);
    }
} 