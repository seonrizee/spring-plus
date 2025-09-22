package org.example.expert.config;

import lombok.Getter;

@Getter
public enum CacheType {

    USER_BY_NICKNAME("userByNickname", 900, 10000); // 15분, 10,000개

    public static final String USER_BY_NICKNAME_CACHE_NAME = "userByNickname";

    private final String cacheName;
    private final int expiredAfterAccess;
    private final int maximumSize;

    CacheType(String cacheName, int expiredAfterAccess, int maximumSize) {
        this.cacheName = cacheName;
        this.expiredAfterAccess = expiredAfterAccess;
        this.maximumSize = maximumSize;
    }
}
