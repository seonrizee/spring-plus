package org.example.expert.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        // 학습을 위해 사용했던 기본 SimpleCacheManager 대신 CaffeineCacheManager 사용
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        for (CacheType cacheType : CacheType.values()) {
            cacheManager.registerCustomCache(cacheType.getCacheName(),
                    Caffeine.newBuilder()
                            .recordStats()
                            .expireAfterAccess(cacheType.getExpiredAfterAccess(), TimeUnit.SECONDS)
                            .maximumSize(cacheType.getMaximumSize())
                            .build());
        }

        return cacheManager;
    }
}
