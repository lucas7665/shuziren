package cn.xfyun.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器
 */
@Controller
public class PageController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/upload";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/files")
    public String files() {
        return "files";
    }

    @GetMapping("/repos")
    public String repos() {
        return "repos";
    }

    @GetMapping("/repo-chat")
    public String repoChat() {
        return "repo-chat";
    }
} 