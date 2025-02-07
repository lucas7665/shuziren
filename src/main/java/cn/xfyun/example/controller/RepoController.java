package cn.xfyun.example.controller;

import cn.xfyun.example.dto.ApiResponse;
import cn.xfyun.example.dto.repo.CreateRepoRequest;
import cn.xfyun.example.service.RepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {
    
    private final RepoService repoService;

    @PostMapping("/create")
    public ApiResponse<?> createRepo(@RequestBody CreateRepoRequest request) {
        log.info("创建知识库: {}", request);
        return repoService.createRepo(request);
    }

    @GetMapping
    public ApiResponse<?> getRepoList() {
        log.info("获取知识库列表");
        return repoService.getRepoList();
    }

    @GetMapping("/{repoId}/files")
    public ApiResponse<?> getRepoFiles(@PathVariable String repoId) {
        log.info("获取知识库文件列表: {}", repoId);
        return repoService.getRepoFiles(repoId);
    }

    @DeleteMapping("/{repoId}")
    public ApiResponse<?> deleteRepo(@PathVariable String repoId) {
        log.info("删除知识库: {}", repoId);
        return repoService.deleteRepo(repoId);
    }

    @DeleteMapping("/{repoId}/files/{fileId}")
    public ApiResponse<?> removeFile(@PathVariable String repoId, @PathVariable String fileId) {
        log.info("从知识库移除文件: repoId={}, fileId={}", repoId, fileId);
        return repoService.removeFile(repoId, fileId);
    }

    @PostMapping("/{repoId}/files/{fileId}")
    public ApiResponse<?> addFile(@PathVariable String repoId, @PathVariable String fileId) {
        log.info("添加文件到知识库: repoId={}, fileId={}", repoId, fileId);
        return repoService.addFileToRepo(repoId, fileId);
    }
} 