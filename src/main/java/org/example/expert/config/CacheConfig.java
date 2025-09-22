package org.example.expert.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(cacheType -> new CaffeineCache(
                                cacheType.getCacheName(),
                                Caffeine.newBuilder()
                                        .recordStats() // 캐시 사용 통계 기록
                                        .expireAfterAccess(cacheType.getExpiredAfterAccess(), TimeUnit.SECONDS)
                                        .maximumSize(cacheType.getMaximumSize())
                                        .build()
                        )
                )
                .collect(Collectors.toList());

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
