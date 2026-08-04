# Realtime Video Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a backend-only `SpringAi-RealtimeVideo` module for AI visual conversation through Spring AI only, without login or local session management.

**Architecture:** The module exposes one REST endpoint for visual assistance. SiliconFlow is accessed through Spring AI's OpenAI-compatible `ChatClient`; no custom HTTP model client, client-secret endpoint, connection endpoint, frame-only endpoint, login, or server-side chat session is kept.

**Tech Stack:** Java 21, Spring Boot Web, Spring Boot validation, Maven, JUnit 5, Mockito.

## Global Constraints

- Do not expose the SiliconFlow API key to the frontend.
- Frontend is out of scope for this phase.
- Keep the module independent from existing `SpringAi-Voice`, `SpringAi-Tool`, and `SpringAi-Milvus` modules.
- Do not add login or server-side video conversation session management in this phase.
- Use Spring AI for model calls. Do not keep custom provider HTTP clients.

---

### Task 1: Module Skeleton

**Files:**
- Create: `SpringAi-RealtimeVideo/pom.xml`
- Create: `SpringAi-RealtimeVideo/src/main/java/com/realtimevideo/RealtimeVideoApplication.java`
- Modify: `pom.xml`
- Test: parent Maven validation

**Interfaces:**
- Produces: Maven module artifact `SpringAi-RealtimeVideo`.

- [ ] Add the module directory and Spring Boot application class.
- [ ] Add `<module>SpringAi-RealtimeVideo</module>` to the parent `pom.xml`.
- [ ] Run `mvn -pl SpringAi-RealtimeVideo test`.

### Task 2: Spring AI Visual Assist Service

**Files:**
- Create: `RealtimeVideoProperties`
- Create: `SpringAiVisualChatClient`
- Create: `ChatClientVisualChatClient`
- Create: `VisualAssistService`
- Test: `VisualAssistServiceTest`

**Interfaces:**
- Consumes: `RealtimeVideoProperties`.
- Produces: `VisualAssistResponse assist(VisualAssistRequest request)`.

- [ ] Write failing tests for delegating text, image data URL, and hint to the Spring AI visual chat client.
- [ ] Implement Spring AI `ChatClient` multimodal call.
- [ ] Run the focused test.

### Task 3: Assist API

**Files:**
- Create: `RealtimeVideoController`
- Test: service/controller tests

**Interfaces:**
- Produces:
  - `POST /api/realtime-video/assist`

- [ ] Expose only `/api/realtime-video/assist`.
- [ ] Remove earlier `connections`, `frames`, and custom HTTP client code.
- [ ] Run focused tests.

### Task 4: Verification

**Files:**
- Modify: `README.md`

- [ ] Document backend endpoints and required config.
- [ ] Run `mvn -pl SpringAi-RealtimeVideo test`.
- [ ] Run `mvn validate` at the parent.
