package com.github.benslabbert.mylinks.factory;

import com.github.benslabbert.mylinks.config.Config;
import com.github.benslabbert.mylinks.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class StorageServiceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceFactory.class);

  private StorageServiceFactory() {}

  public static final JedisPool POOL;
  public static final StorageService INSTANCE;

  static {
    var cfg = Config.INSTANCE;

    var poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(4);
    poolConfig.setMaxIdle(4);
    POOL = new JedisPool(poolConfig, cfg.redis().host(), cfg.redis().port());

    INSTANCE = new StorageService(POOL);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOGGER.info("closing instance");
                  POOL.close();
                }));
  }
}
