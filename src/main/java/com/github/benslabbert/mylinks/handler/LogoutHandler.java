package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class LogoutHandler implements RequestHandler {

  public static final String PATH = "/logout";

  private static final Logger log = LoggerFactory.getLogger(LogoutHandler.class);

  private final JedisPool jedisPool;

  public LogoutHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    String userId = request.headers().get(CustomHeaders.USER_ID.val());
    String token = request.headers().get(CustomHeaders.TOKEN.val());

    try (var jedis = jedisPool.getResource()) {
      var s = jedis.get(userId + "-token");
      if (StringUtils.isEmpty(s)) {
        throw new UnauthorizedException("bad username");
      }

      if (!s.equals(token)) {
        throw new UnauthorizedException("bad token");
      }

      jedis.del(userId + "-token");
      return new Response(NO_CONTENT, InputStream.nullInputStream());
    }
  }
}
