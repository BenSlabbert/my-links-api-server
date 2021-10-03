package com.github.benslabbert.mylinks.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import java.util.concurrent.CompletableFuture;
import redis.clients.jedis.JedisPool;

public class AuthHandler implements RequestHandler {

  private final JedisPool jedisPool;

  public AuthHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public CompletableFuture<Response> handle(CompletableFuture<FullHttpRequest> future) {
    // todo add auth
    return null;
  }
}
