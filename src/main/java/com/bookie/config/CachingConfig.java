package com.bookie.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig implements CachingConfigurer {
  @Override
  public CacheErrorHandler errorHandler() {
    return new LoggingCacheErrorHandler();
  }
}
