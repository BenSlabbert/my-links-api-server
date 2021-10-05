package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;

import com.github.benslabbert.mylinks.exception.ConflictException;
import com.github.benslabbert.mylinks.util.BasicAuthUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class CreateAccountHandler implements RequestHandler {

  public static final String PATH = "/create_account";

  private static final Logger log = LoggerFactory.getLogger(CreateAccountHandler.class);

  private final JedisPool jedisPool;

  public CreateAccountHandler(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    var credentials = BasicAuthUtil.getCredentials(request);

    try (var jedis = jedisPool.getResource()) {
      var s = jedis.get(credentials.username());

      if (!StringUtils.isEmpty(s)) {
        throw new ConflictException("unable to create account");
      }

      jedis.set(credentials.username(), credentials.password());

      var token = UUID.randomUUID().toString();
      jedis.set(credentials.username() + "-token", token);
      return new Response(
          CREATED, new ByteArrayInputStream(token.getBytes(StandardCharsets.UTF_8)));
    }
  }
}
