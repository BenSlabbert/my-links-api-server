package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class UriHandler implements RequestHandler {

  private static final Logger log = LoggerFactory.getLogger(UriHandler.class);

  private final JedisPool jedisPool;

  public UriHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public CompletableFuture<Response> handle(CompletableFuture<FullHttpRequest> future) {
    return future.thenComposeAsync(
        request -> {
          var userId = request.headers().get(CustomHeaders.USER_ID.val());
          var token = request.headers().get(CustomHeaders.TOKEN.val());
          // this is an authenticated request
          try (var jedis = jedisPool.getResource()) {
            var storedToken = jedis.get(userId);
            if (StringUtils.isEmpty(storedToken)) {
              throw new UnauthorizedException("user not found");
            }

            if (!storedToken.equals(token)) {
              throw new UnauthorizedException("invalid token");
            }
          }

          return switch (request.method().name()) {
            case "GET" -> get(supplyAsync(() -> request));
            case "POST" -> post(supplyAsync(() -> request));
            default -> completedFuture(
                new Response(METHOD_NOT_ALLOWED, InputStream.nullInputStream()));
          };
        });
  }

  private CompletableFuture<Response> get(CompletableFuture<FullHttpRequest> future) {
    return future.thenComposeAsync(
        request -> {
          var reqId = request.headers().get(CustomHeaders.REQ_ID.val());
          log.info("{} handle get", reqId);

          var data = RandomStringUtils.randomAlphabetic(1024).getBytes(StandardCharsets.UTF_8);
          return completedFuture(
              new Response(HttpResponseStatus.OK, new ByteArrayInputStream(data)));
        });
  }

  private CompletableFuture<Response> post(CompletableFuture<FullHttpRequest> future) {
    return future.thenComposeAsync(
        request -> {
          var reqId = request.headers().get(CustomHeaders.REQ_ID.val());
          log.info("{} handle post", reqId);

          var content = request.content();
          var bodyStr = content.toString(StandardCharsets.UTF_8);
          content.release();

          log.info("body {}", bodyStr);
          return completedFuture(
              new Response(HttpResponseStatus.OK, InputStream.nullInputStream()));
        });
  }
}
