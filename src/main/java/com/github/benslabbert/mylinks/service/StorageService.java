package com.github.benslabbert.mylinks.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import redis.clients.jedis.JedisPool;

public class StorageService {

  private final JedisPool jedisPool;

  public StorageService(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  public Optional<byte[]> getUser(String key) {
    try (var jedis = jedisPool.getResource()) {
      return Optional.ofNullable(jedis.get(key.getBytes(StandardCharsets.UTF_8)));
    }
  }

  public Optional<String> getToken(String userId) {
    try (var jedis = jedisPool.getResource()) {
      return Optional.ofNullable(jedis.get(userId + "-token"));
    }
  }

  public void setToken(String userId, UUID token) {
    try (var jedis = jedisPool.getResource()) {
      jedis.set(userId + "-token", token.toString());
    }
  }

  public void removeToken(String userId) {
    try (var jedis = jedisPool.getResource()) {
      jedis.del(userId + "-token");
    }
  }

  public void setUser(String key, String password) {
    try (var jedis = jedisPool.getResource()) {
      jedis.set(key, password);
    }
  }
}
