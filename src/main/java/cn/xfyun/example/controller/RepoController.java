package cn.xfyun.example.controller;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.CreateRepoRequest;
import cn.xfyun.example.service.RepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@Tag(name = "知识库管理", description = "包含知识库的创建、查询、删除，以及文件的添加和移除等操作")
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {
    
    private final RepoService repoService;

    @Operation(summary = "创建知识库", 
        description = "创建一个新的知识库，用于存储和管理文档。\n\n" +
            "使用示例：\n" +
            "```json\n" +
            "{\n" +
            "    \"repoName\": \"产品文档库\",\n" +
            "    \"repoDesc\": \"存放所有产品相关的文档\",\n" +
            "    \"repoTags\": \"产品,文档,API\"\n" +
            "}\n" +
            "```\n\n" +
            "使用流程：\n" +
            "1. 创建知识库\n" +
            "2. 上传文件并等待向量化完成\n" +
            "3. 将向量化完成的文件添加到知识库\n" +
            "4. 使用知识库进行问答",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
        })
    @PostMapping("/create")
    public ApiResponse<?> createRepo(@RequestBody CreateRepoRequest request) {
        log.info("创建知识库: {}", request);
        return repoService.createRepo(request);
    }

    @Operation(summary = "获取知识库列表",
        description = "获取当前应用下的所有知识库列表，包含知识库的基本信息")
    @GetMapping
    public ApiResponse<?> getRepoList() {
        log.info("获取知识库列表");
        return repoService.getRepoList();
    }

    @Operation(summary = "获取知识库文件列表",
        description = "获取指定知识库中的所有文件列表，包含文件的处理状态",
        parameters = {
            @Parameter(name = "repoId", description = "知识库ID", required = true)
        })
    @GetMapping("/{repoId}/files")
    public ApiResponse<?> getRepoFiles(@PathVariable String repoId) {
        log.info("获取知识库文件列表: {}", repoId);
        return repoService.getRepoFiles(repoId);
    }

    @Operation(summary = "删除知识库",
        description = "删除指定的知识库，同时会解除与文件的关联关系，但不会删除文件本身",
        parameters = {
            @Parameter(name = "repoId", description = "知识库ID", required = true)
        })
    @DeleteMapping("/{repoId}")
    public ApiResponse<?> deleteRepo(@PathVariable String repoId) {
        log.info("删除知识库: {}", repoId);
        return repoService.deleteRepo(repoId);
    }

    @Operation(summary = "从知识库移除文件",
        description = "从指定知识库中移除文件，不会删除文件本身",
        parameters = {
            @Parameter(name = "repoId", description = "知识库ID", required = true),
            @Parameter(name = "fileId", description = "文件ID", required = true)
        })
    @DeleteMapping("/{repoId}/files/{fileId}")
    public ApiResponse<?> removeFile(@PathVariable String repoId, @PathVariable String fileId) {
        log.info("从知识库移除文件: repoId={}, fileId={}", repoId, fileId);
        return repoService.removeFile(repoId, fileId);
    }

    @Operation(summary = "添加文件到知识库",
        description = "将已上传并向量化的文件添加到指定的知识库中。注意：只有状态为vectored的文件才能添加",
        parameters = {
            @Parameter(name = "repoId", description = "知识库ID", required = true),
            @Parameter(name = "fileId", description = "文件ID", required = true)
        })
    @PostMapping("/{repoId}/files/{fileId}")
    public ApiResponse<?> addFile(@PathVariable String repoId, @PathVariable String fileId) {
        log.info("添加文件到知识库: repoId={}, fileId={}", repoId, fileId);
        return repoService.addFileToRepo(repoId, fileId);
    }
} 