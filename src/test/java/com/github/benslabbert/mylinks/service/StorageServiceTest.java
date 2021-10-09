package com.github.benslabbert.mylinks.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

class StorageServiceTest {

  private static final GenericContainer<?> REDIS =
      new GenericContainer<>("redis:6-alpine")
          .withExposedPorts(6379)
          .waitingFor(Wait.forListeningPort());
  private static int REDIS_PORT = 0;

  private StorageService storageService;
  private JedisPool jedisPool;

  @BeforeAll
  static void beforeAll() {
    REDIS.start();
    REDIS_PORT = REDIS.getMappedPort(6379);
  }

  @BeforeEach
  void before() {
    var poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(4);
    poolConfig.setMaxIdle(4);
    jedisPool = new JedisPool(poolConfig, "127.0.0.1", REDIS_PORT);

    try (var jedis = jedisPool.getResource()) {
      assertThat(jedis.ping()).isEqualTo("PONG");
    }

    storageService = new StorageService(jedisPool);
  }

  @AfterEach
  void after() {
    jedisPool.close();
  }

  @Test
  void test() {
    var uname = RandomStringUtils.randomAlphabetic(6);
    var user = storageService.createUser(uname, "password");
    assertThat(user).isNotNull();

    var storedUser = storageService.getUser(uname);
    assertThat(storedUser).isPresent();
    assertThat(storedUser.get().id()).isEqualTo(user.id());
    assertThat(storedUser.get().username()).isEqualTo(user.username());
    assertThat(storedUser.get().password()).isEqualTo(user.password());
  }
}
