package com.github.benslabbert.mylinks.util;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;

public class BasicAuthUtil {

  private static final String BASIC_HEADER_PREFIX = "Basic ";

  private BasicAuthUtil() {}

  public static Credentials getCredentials(FullHttpRequest request) {
    var basicAuth = request.headers().get(HttpHeaderNames.AUTHORIZATION);

    if (StringUtils.isEmpty(basicAuth)) {
      throw new UnauthorizedException(
          "required header not provided: " + HttpHeaderNames.AUTHORIZATION);
    }

    if (!basicAuth.startsWith(BASIC_HEADER_PREFIX)) {
      throw new UnauthorizedException("invalid header value: " + HttpHeaderNames.AUTHORIZATION);
    }

    basicAuth = basicAuth.substring(BASIC_HEADER_PREFIX.length());

    var arr = new String(Base64.getDecoder().decode(basicAuth), StandardCharsets.UTF_8).split(":");

    if (arr.length != 2) {
      throw new UnauthorizedException("invalid header value: " + HttpHeaderNames.AUTHORIZATION);
    }

    return new Credentials(arr[0], arr[1]);
  }

  public static record Credentials(String username, String password) {}
}
