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
