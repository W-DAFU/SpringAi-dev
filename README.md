# SpringAi-dev

`SpringAi-dev` 是 Spring AI 练习项目集合。根目录作为 Maven 父级/聚合项目使用，每个子模块都是独立的练习项目，子模块自己按需引入依赖。

## 项目结构

```text
SpringAi-dev/
|-- pom.xml
|-- README.md
`-- SpringAi-Voice/
```

## 子模块说明

| 子模块 | 作用 | 主要能力 |
| --- | --- | --- |
| `SpringAi-Voice` | Spring AI 语音能力练习项目 | 文本聊天、语音转文字、可选的文字转语音回复 |


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

## SpringAi-RealtimeVideo

`SpringAi-RealtimeVideo` 是实时视频对话后端模块，当前只开放后端能力，不包含 Vue 前端。

主要职责：

- 提供无登录、无本地会话的实时视频对话后端接口。
- 统一使用 Spring AI 的 OpenAI-compatible 配置接入 SiliconFlow，后端持有 `SILICONFLOW_API_KEY`。
- 不做登录和会话管理，前端点击按钮后直接调用视觉助手接口。
- 接收用户语音、文字和摄像头截图，分别交给 Spring AI ASR、ChatClient 和 TTS 完成视频对话链路。

配置：

```yaml
server:
  port: 8084

app:
  realtime-video:
    instructions: |
      你是一个实时视觉对话助手。
```

同时通过 Spring AI 配置模型：

```yaml
spring:
  ai:
    model:
      chat: openai
      audio:
        speech: openai
        transcription: openai
    openai:
      api-key: ${SILICONFLOW_API_KEY:siliconflow}
      base-url: https://api.siliconflow.cn/v1
      chat:
        model: Qwen/Qwen3-VL-32B-Instruct
      audio:
        speech:
          model: FunAudioLLM/CosyVoice2-0.5B
          voice: fnlp/MOSS-TTSD-v0.5:alex
          response-format: mp3
        transcription:
          model: TeleAI/TeleSpeechASR
          response-format: json
```

接口：

```http
POST /api/realtime-video/speech-to-text
Content-Type: multipart/form-data

audioFile=@user.wav
```

```http
POST /api/realtime-video/assist
Content-Type: application/json

{
  "userText": "帮我看看这个设备为什么没反应",
  "imageDataUrl": "data:image/jpeg;base64,...",
  "userHint": "镜头里是设备正面的指示灯"
}
```

`/assist` 内部通过 Spring AI `ChatClient` 调用模型；传入 `imageDataUrl` 时，会解析成 `ByteArrayResource + MimeType` 作为多模态图片输入。

```http
POST /api/realtime-video/text-to-speech
Content-Type: application/json

{
  "text": "请把镜头靠近设备正面的指示灯。"
}
```
