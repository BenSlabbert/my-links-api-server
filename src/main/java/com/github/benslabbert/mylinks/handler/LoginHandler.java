package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import com.github.benslabbert.mylinks.util.BasicAuthUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class LoginHandler implements RequestHandler {

  public static final String PATH = "/login";

  private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

  private final JedisPool jedisPool;

  public LoginHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    var credentials = BasicAuthUtil.getCredentials(request);

    try (var jedis = jedisPool.getResource()) {
      var s = jedis.get(credentials.username());

      if (StringUtils.isEmpty(s)) {
        throw new UnauthorizedException("unknown account");
      }

      if (!s.equals(credentials.password())) {
        throw new UnauthorizedException("bad password");
      }

      var token = UUID.randomUUID().toString();
      jedis.set(credentials.username() + "-token", token);
      return new Response(OK, new ByteArrayInputStream(token.getBytes(StandardCharsets.UTF_8)));
    }
  }
}
