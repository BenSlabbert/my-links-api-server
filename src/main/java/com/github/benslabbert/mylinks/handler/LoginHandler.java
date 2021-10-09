package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.service.StorageService;
import com.github.benslabbert.mylinks.util.BasicAuthUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler implements RequestHandler {

  public static final String PATH = "/login";

  private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

  private final StorageService storageService;

  public LoginHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    var credentials = BasicAuthUtil.getCredentials(request);

    var s = storageService.getUser(credentials.username());

    if (s.isEmpty()) {
      return Response.unauthorized();
    }

    var storedPassword = new String(s.get(), StandardCharsets.UTF_8);
    if (!storedPassword.equals(credentials.password())) {
      return Response.unauthorized();
    }

    var token = UUID.randomUUID();
    storageService.setToken(credentials.username(), token);

    return Response.ok(new ByteArrayInputStream(token.toString().getBytes(StandardCharsets.UTF_8)));
  }
}
