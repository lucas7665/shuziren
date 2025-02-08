# 智能问答系统

基于讯飞星火大模型的智能问答系统，支持文档上传、知识库管理和智能问答等功能。

## 核心功能

1. 文档管理
- 支持上传 PDF、Word、TXT 等格式文档
- 自动进行文档向量化处理
- 查看文档处理状态和管理文档

2. 知识库管理
- 创建和管理多个知识库
- 为知识库添加/移除文档
- 设置知识库描述和标签

3. 智能问答
- 基于知识库的精准问答
- 支持多轮对话上下文
- 文档相似度匹配和过滤
- 大模型参数调优
- 显示答案的参考来源文档
- 支持大模型兜底回答

## 技术特性

1. 问答优化
- 文本相似度阈值控制(wikiFilterScore)
- 大模型温度参数调节(temperature)
- 自定义问答提示模板(wikiPromptTpl)
- 大模型兜底开关(spark)

2. 接口能力
- WebSocket 实时问答
- RESTful API 接口
- Swagger API 文档
- 统一响应格式

## 快速开始

1. 配置文件
```yaml
xfyun:
  appId: 你的讯飞应用ID
  secret: 你的讯飞密钥
  domain: https://knowledge.xfyun.cn
  chatUrl: wss://knowledge.xfyun.cn/v1/chat
```

2. 启动应用
```bash
mvn spring-boot:run
```

3. 访问地址
- 主页: http://localhost:8080
- Swagger文档: http://localhost:8080/swagger-ui.html

## 使用流程

1. 文档上传
- 访问"文档上传"页面
- 选择并上传文档
- 等待文档向量化完成

2. 知识库管理
- 创建新知识库
- 选择已向量化的文档
- 添加到知识库

3. 智能问答
- 选择要查询的知识库
- 输入问题
- 查看AI回答和参考来源

## 高级配置

可以在问答界面调整以下参数：
- 结果分数阈值(0-1): 控制文档匹配精度
- 查询数量(1-200): 设置匹配文档数量
- 大模型温度(0-1): 调节回答的随机性
- 大模型兜底: 未匹配到文档时使用大模型回答
- 问答提示模板: 自定义问答格式

## 注意事项

1. 文档处理
- 支持格式: PDF、DOC、DOCX、TXT
- 单文件大小限制: 10MB
- 等待向量化完成才能添加到知识库

2. 问答优化
- 建议先设置较高的相似度阈值(如0.8)
- 根据实际效果调整参数
- 可以通过日志查看匹配详情

## 开发计划

- [ ] 支持更多文档格式
- [ ] 添加用户管理功能
- [ ] 优化文档处理速度
- [ ] 增加批量导入功能
- [ ] 添加数据统计分析

## 常用接口说明

### 1. 知识库问答接口
```http
POST /api/repo-chat

{
    "repoId": "知识库ID",
    "question": "用户问题",
    "topN": 3,
    "chatExtends": {
        "wikiPromptTpl": "基于以下内容回答问题：\n<wikicontent>\n\n问题：<wikiquestion>\n回答：",
        "wikiFilterScore": 0.82,
        "spark": true,
        "temperature": 0.5
    }
}
```

响应格式：
```json
{
    "success": true,
    "data": {
        "answer": "AI的回答内容...",
        "references": {
            "files": [
                "参考文档1.pdf",
                "参考文档2.docx"
            ]
        }
    }
}
```

### 2. 知识库管理接口

#### 创建知识库
```http
POST /api/repos

{
    "repoName": "知识库名称",
    "repoDesc": "知识库描述",
    "repoTags": "标签1,标签2"
}
```

#### 获取知识库列表
```http
GET /api/repos
```

#### 删除知识库
```http
DELETE /api/repos/{repoId}
```

#### 添加文档到知识库
```http
POST /api/repos/{repoId}/files/{fileId}
```

#### 从知识库移除文档
```http
DELETE /api/repos/{repoId}/files/{fileId}
```

### 3. 文档管理接口

#### 上传文档
```http
POST /api/files
Content-Type: multipart/form-data

file: 文件内容
```

#### 获取文档状态
```http
GET /api/files/{fileId}/status
```

### 4. 接口使用建议

1. 知识库问答
- 首次调用建议关闭spark，确保答案来自知识库
- 可以通过调整wikiFilterScore来控制匹配精度
- 建议保留默认的提示模板，除非有特殊需求

2. 文档处理
- 上传文档后要等待向量化完成
- 可以通过状态接口查询处理进度
- 只有vectored状态的文档才能添加到知识库

3. 错误处理
- 所有接口都使用统一的响应格式
- success字段表示是否成功
- 失败时message字段包含错误信息
