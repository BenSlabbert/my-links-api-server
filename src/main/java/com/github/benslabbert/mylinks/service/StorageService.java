package com.github.benslabbert.mylinks.service;

import static com.github.benslabbert.mylinks.util.Encoder.encode;
import static com.github.benslabbert.mylinks.util.Encoder.encodeUUID;

import com.github.benslabbert.mylinks.exception.ConflictException;
import com.github.benslabbert.mylinks.persistence.UserPo;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import redis.clients.jedis.JedisPool;

public record StorageService(JedisPool jedisPool) {

  private static final byte[] USERS_HSET_KEY = encode("users");
  private static final byte[] TOKEN_HSET_KEY = encode("tokens");
  private static final byte[] USER_URIS_HSET_KEY_PREFIX = encode("uris-");

  public Optional<UUID> getToken(UUID userId) {
    try (var jedis = jedisPool.getResource()) {
      var token = Optional.ofNullable(jedis.hget(TOKEN_HSET_KEY, encodeUUID(userId)));

      if (token.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(encodeUUID(token.get()));
    }
  }

  public void setToken(UUID userId, UUID token) {
    try (var jedis = jedisPool.getResource()) {
      jedis.hset(TOKEN_HSET_KEY, encodeUUID(userId), encodeUUID(token));
    }
  }

  public void removeToken(UUID userId) {
    try (var jedis = jedisPool.getResource()) {
      jedis.hdel(TOKEN_HSET_KEY, encodeUUID(userId));
    }
  }

  public UserPo createUser(String username, String password) {
    var userId = UUID.randomUUID();

    try (var jedis = jedisPool.getResource()) {
      // check if username already exists
      Long set = jedis.hsetnx(USERS_HSET_KEY, encode(username), encodeUUID(userId));
      if (set == 0L) {
        // username already exists
        throw new ConflictException("username already exists: " + username);
      }

      // now create the user
      var user = new UserPo(userId, username, password);

      // we don't care about the responses
      var p = jedis.pipelined();
      p.hset(encodeUUID(userId), user.serialize());
      p.hset(TOKEN_HSET_KEY, encodeUUID(userId), encodeUUID(UUID.randomUUID()));
      p.sync();
      return user;
    }
  }

  public Optional<UserPo> getUser(String username) {
    try (var jedis = jedisPool.getResource()) {
      var userIdBytes = Optional.ofNullable(jedis.hget(USERS_HSET_KEY, encode(username)));

      if (userIdBytes.isEmpty()) {
        return Optional.empty();
      }

      var hmap = jedis.hgetAll(userIdBytes.get());
      return Optional.of(UserPo.fromHmap(hmap));
    }
  }

  public UUID addUri(UUID userId, String uri) {
    try (var jedis = jedisPool.getResource()) {
      var entryId = UUID.randomUUID();
      jedis.hset(getUserUriKey(userId), encodeUUID(entryId), encode(uri));
      return entryId;
    }
  }

  public Map<UUID, String> getUris(UUID userId) {
    try (var jedis = jedisPool.getResource()) {
      return jedis.hgetAll(getUserUriKey(userId)).entrySet().stream()
          .collect(Collectors.toMap(e -> encodeUUID(e.getKey()), e -> encode(e.getValue())));
    }
  }

  private byte[] getUserUriKey(UUID userId) {
    byte[] bytes = encodeUUID(userId);
    byte[] merge = new byte[USER_URIS_HSET_KEY_PREFIX.length + bytes.length];
    System.arraycopy(USER_URIS_HSET_KEY_PREFIX, 0, merge, 0, USER_URIS_HSET_KEY_PREFIX.length);
    System.arraycopy(bytes, 0, merge, USER_URIS_HSET_KEY_PREFIX.length, bytes.length);
    return merge;
  }
}
