package com.df.tool.cardresponse.session;

import com.df.tool.cardresponse.domain.CardSessionPayload;

import java.util.Optional;

/**
 * 卡片案例的会话缓存。
 */
public interface CardSessionCache {

    /**
     * 生成 Redis key。
     */
    String key(String userId);

    /**
     * 保存工具生成的协议组装数据。
     */
    void save(String userId, CardSessionPayload payload);

    /**
     * 读取工具保存的协议组装数据。
     */
    Optional<CardSessionPayload> get(String userId);

    /**
     * 删除用户本次会话缓存。
     */
    void delete(String userId);
}
