# SpringAi-Voice

`SpringAi-Voice` 是一个基于 Spring Boot 和 Spring AI 的语音聊天练习模块，提供普通一次性聊天接口和流式聊天接口。

## 项目功能

- 支持文字聊天：用户传入文本，后端调用聊天模型生成回复。
- 支持语音聊天：用户上传音频文件，后端先进行语音转文字，再调用聊天模型。
- 支持回复转语音：请求传入 `ttsEnabled=true` 时，后端把 AI 文本回复转换成音频并返回 Base64。
- 支持流式输出：后端直接调用 OpenAI 兼容 Chat Completions SSE 接口，读取 `data:` 行中的 `choices[0].delta.content`，再按 NDJSON 协议解析成完整业务 block 后推送给前端。
- 支持商品卡片 block：流式接口可返回 `product_card` 类型 block，后端会根据模型给出的 query 模拟查询商品接口，并把商品 JSON 返回给前端渲染。

## 目录说明

```text
src/main/java/com/voice
├── commons                 # 普通接口和流式接口共用逻辑
├── controller              # HTTP 接口入口
├── domain                  # 请求参数和响应对象
├── handler                 # 全局异常处理
└── service
    ├── chat                # 普通非流式聊天服务
    └── stream              # 流式聊天服务、解析器、block 组装器
```

## 通用请求参数

两个聊天接口都使用 `multipart/form-data` 接收参数。

| 参数名 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `sessionId` | String | 否 | 会话 ID，用于前端区分不同会话，后续可用于多轮上下文。 |
| `messageType` | String | 是 | 消息类型。可选值：`TEXT`、`AUDIO`。 |
| `text` | String | `messageType=TEXT` 时必填 | 用户输入的文字内容。 |
| `audioFile` | File | `messageType=AUDIO` 时必填 | 用户上传的音频文件，后端会先语音转文字。 |
| `ttsEnabled` | Boolean | 否 | 是否把 AI 回复转成语音，默认 `false`。 |

## 普通聊天接口

### 基本信息

- 请求方式：`POST`
- 请求路径：`/api/chat`
- 请求类型：`multipart/form-data`
- 响应类型：`application/json`
- 说明：一次性返回完整 AI 回复。

### 请求示例

文字聊天：

```bash
curl -X POST http://localhost:8080/api/chat \
  -F "messageType=TEXT" \
  -F "text=推荐一款儿童手表" \
  -F "sessionId=session-001" \
  -F "ttsEnabled=true"
```

语音聊天：

```bash
curl -X POST http://localhost:8080/api/chat \
  -F "messageType=AUDIO" \
  -F "audioFile=@./test-audio.wav" \
  -F "sessionId=session-001" \
  -F "ttsEnabled=true"
```

### 响应结构

普通接口统一返回 `R<ChatMessageVo>`。

```json
{
  "code": 200,
  "message": "success",
  "success": true,
  "data": {
    "sessionId": "session-001",
    "messageType": "TEXT",
    "inputText": "推荐一款儿童手表",
    "answerText": "可以考虑华为儿童手表5活力版。",
    "answerAudioFormat": "mp3",
    "answerAudioBase64": "base64音频内容"
  }
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `code` | Integer | 状态码，`200` 表示成功。 |
| `message` | String | 响应消息，成功时为 `success`。 |
| `success` | Boolean | 请求是否成功。 |
| `data.sessionId` | String | 本次会话 ID。 |
| `data.messageType` | String | 本次用户输入类型：`TEXT` 或 `AUDIO`。 |
| `data.inputText` | String | 实际送入聊天模型的文本。语音输入时为语音识别结果。 |
| `data.answerText` | String | AI 模型返回的完整文本回复。 |
| `data.answerAudioFormat` | String | 回复音频格式。仅 `ttsEnabled=true` 时有值，目前为 `mp3`。 |
| `data.answerAudioBase64` | String | 回复音频 Base64 内容。仅 `ttsEnabled=true` 时有值。 |

## 流式聊天接口

### 基本信息

- 请求方式：`POST`
- 请求路径：`/api/chat/stream`
- 请求类型：`multipart/form-data`
- 响应类型：`text/event-stream`
- 说明：使用 SSE 流式返回已经解析完成的业务 block，不套用普通 `R` 响应包装。

### 请求示例

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -F "messageType=TEXT" \
  -F "text=推荐两款儿童手表" \
  -F "sessionId=session-002" \
  -F "ttsEnabled=true"
```

### 模型输出协议

流式服务要求模型只输出 NDJSON：一行一个完整 JSON block。后端不会把模型原始 SSE 直接转发给前端，而是先从上游 SSE 中提取 content 增量，再解析、组装成 `ChatStreamBlockVo`。

```json
{"type":"text","text":"1. 华为儿童手表5活力版\n\n- 核心功能：定位、防水。"}
{"type":"product_card","query":"华为儿童手表5活力版"}
{"type":"text","text":"2. 小天才Q3A\n\n- 核心功能：AI定位、视频通话。"}
```

后端解析规则：

- 真实换行符表示一个 block 结束。
- JSON 字符串字段内部换行必须写成 `\n`，不能输出真实换行。
- `type=text` 时读取 `text` 字段。
- `type=product_card` 时读取 `query` 字段，后端会把该 query 作为查询条件，模拟调用商品卡片接口。

### SSE 响应事件

流式接口使用 Server-Sent Events。后端每解析并组装好一个完整 JSON block，就立即发送一个 `block` 事件。

文本 block 推送示例：

```text
event: block
data: {"sessionId":"session-002","messageType":"TEXT","inputText":"推荐两款儿童手表","answerText":"1. 华为儿童手表5活力版\n\n- 核心功能：定位、防水。","answerAudioFormat":"mp3","answerAudioBase64":"base64音频内容","cardType":"text"}
```

商品卡片 block 推送示例：

```text
event: block
data: {"sessionId":"session-002","messageType":"TEXT","inputText":"推荐两款儿童手表","answerText":"{\"query\":\"华为儿童手表5活力版\",\"name\":\"华为儿童手表5活力版\",\"summary\":\"模拟商品卡片数据，后续可替换为真实接口返回结果。\",\"tags\":[\"AI推荐\",\"可渲染卡片\"],\"source\":\"mock\"}","answerAudioFormat":null,"answerAudioBase64":null,"cardType":"product_card"}
```

流正常结束时：

```text
event: done
data: {"finished":true}
```

流处理失败时：

```text
event: error
data: {"message":"错误说明"}

event: done
data: {"finished":false}
```

### 流式响应结构

流式接口返回 `ChatStreamBlockVo`，它继承 `ChatMessageVo`，并额外增加 `cardType` 字段。

文本 block 示例：

```json
{
  "sessionId": "session-002",
  "messageType": "TEXT",
  "inputText": "推荐两款儿童手表",
  "answerText": "1. 华为儿童手表5活力版\n\n- 核心功能：定位、防水。",
  "answerAudioFormat": "mp3",
  "answerAudioBase64": "base64音频内容",
  "cardType": "text"
}
```

商品卡片 block 示例：

```json
{
  "sessionId": "session-002",
  "messageType": "TEXT",
  "inputText": "推荐两款儿童手表",
  "answerText": "{\"query\":\"华为儿童手表5活力版\",\"name\":\"华为儿童手表5活力版\",\"summary\":\"模拟商品卡片数据，后续可替换为真实接口返回结果。\",\"tags\":[\"AI推荐\",\"可渲染卡片\"],\"source\":\"mock\"}",
  "answerAudioFormat": null,
  "answerAudioBase64": null,
  "cardType": "product_card"
}
```

### 流式响应字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `sessionId` | String | 本次会话 ID。 |
| `messageType` | String | 本次用户输入类型：`TEXT` 或 `AUDIO`。 |
| `inputText` | String | 实际送入聊天模型的文本。语音输入时为语音识别结果。 |
| `answerText` | String | 当前 block 的内容。`cardType=text` 时为展示文本；`cardType=product_card` 时为后端查询后的商品卡片 JSON 字符串。 |
| `answerAudioFormat` | String | 当前文本 block 的回复音频格式。仅 `cardType=text` 且 `ttsEnabled=true` 时有值。 |
| `answerAudioBase64` | String | 当前文本 block 的回复音频 Base64 内容。仅 `cardType=text` 且 `ttsEnabled=true` 时有值。 |
| `cardType` | String | 当前 block 类型。可选值：`text`、`product_card`。 |

## 错误响应

普通接口和参数校验错误会返回统一结构：

```json
{
  "code": 400,
  "message": "文本不能为空",
  "success": false,
  "data": null
}
```

常见错误：

| 场景 | 错误说明 |
| --- | --- |
| 未传 `messageType` | 消息类型不能为空。 |
| `messageType=TEXT` 但未传 `text` | 文本不能为空。 |
| `messageType=AUDIO` 但未传 `audioFile` | 音频不能为空。 |
| 流式模型输出不是合法 NDJSON | 解析流式 block 失败。 |

## 本地验证

在父工程目录执行：

```bash
mvn -pl SpringAi-Voice test
mvn validate
```
