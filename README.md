# SpringAi-dev

`SpringAi-dev` 是 Spring AI 练习项目集合。根目录作为 Maven 父级/聚合项目使用，每个子模块都是独立的练习项目，子模块自己按需引入依赖。

## 项目结构

```text
SpringAi-dev/
|-- pom.xml
|-- README.md
`-- SpringAi-Voice/
    |-- pom.xml
    `-- src/
```

## 子模块说明

| 子模块 | 作用 | 主要能力 |
| --- | --- | --- |
| `SpringAi-Voice` | Spring AI 语音能力练习项目 | 文本聊天、语音转文字、可选的文字转语音回复 |

### SpringAi-Voice

`SpringAi-Voice` 是一个基于 Spring Boot 和 Spring AI 的语音交互练习模块。

当前功能：

- 接收文本输入，并发送给聊天模型生成回复。
- 接收上传的音频文件，并将语音转写为文本。
- 支持把 AI 的文本回复转换为音频。
- 使用 OpenAI 兼容接口配置，当前配置指向 `application.yml` 中的 SiliconFlow 地址。

主要入口：

- 启动类：`com.ye.SpringAiYe01Application`
- 接口控制器：`com.ye.controller.SpringAiController`
- 接口地址：`POST /api/chat`
- 请求格式：`multipart/form-data`

## 添加新的练习模块

在 `SpringAi-dev` 下创建新的子模块目录，然后把模块名添加到根目录 `pom.xml`：

```xml
<modules>
    <module>SpringAi-Voice</module>
    <module>your-module-name</module>
</modules>
```

每个子模块在自己的 `pom.xml` 中管理依赖。父级只负责聚合模块，不统一引入具体业务依赖。

## 验证

在根目录验证所有已聚合的模块：

```powershell
mvn validate
```

只验证单个模块：

```powershell
cd SpringAi-Voice
mvn test
```
