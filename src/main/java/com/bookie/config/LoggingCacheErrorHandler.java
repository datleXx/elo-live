package com.bookie.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class LoggingCacheErrorHandler implements CacheErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

  @Override
  public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
    log.warn("Cache get failed for {} key {}", cache.getName(), key, exception);
    // swallow - the method body will run instead
  }

  @Override
  public void handleCachePutError(
      RuntimeException exception, Cache cache, Object key, Object value) {
    log.warn("Cache put failed for {} key {}", cache.getName(), key, exception);
  }

  @Override
  public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
    log.warn("Cache evict failed for {} key {}", cache.getName(), key, exception);
  }

  @Override
  public void handleCacheClearError(RuntimeException exception, Cache cache) {
    log.warn("Cache clear failed for {}", cache.getName(), exception);
  }
}
