package com.realtimevideo.service;

public interface SpringAiVisualChatClient {

    String complete(String instructions, String userText, String imageDataUrl, String userHint);
}
