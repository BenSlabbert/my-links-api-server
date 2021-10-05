package com.github.benslabbert.mylinks.handler;

import static com.github.benslabbert.mylinks.handler.CustomHeaders.REQ_ID;
import static com.github.benslabbert.mylinks.handler.CustomHeaders.TOKEN;
import static com.github.benslabbert.mylinks.handler.CustomHeaders.USER_ID;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class UriHandler implements RequestHandler {

  public static final String PATH = "/uris";

  private static final Logger log = LoggerFactory.getLogger(UriHandler.class);

  private final JedisPool jedisPool;

  public UriHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");
    var userId = request.headers().get(USER_ID.val());
    var token = request.headers().get(TOKEN.val());
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
      case "GET" -> get(request);
      case "POST" -> post(request);
      default -> new Response(METHOD_NOT_ALLOWED, InputStream.nullInputStream());
    };
  }

  private Response get(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    log.info("{} handle get", reqId);

    var data = RandomStringUtils.randomAlphabetic(1024).getBytes(UTF_8);
    return new Response(OK, new ByteArrayInputStream(data));
  }

  private Response post(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    log.info("{} handle post", reqId);

    var content = request.content();
    var bodyStr = content.toString(UTF_8);
    content.release();

    log.info("body {}", bodyStr);
    return new Response(OK, InputStream.nullInputStream());
  }
}
