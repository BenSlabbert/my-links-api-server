package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutHandler implements RequestHandler {

  public static final String PATH = "/logout";

  private static final Logger log = LoggerFactory.getLogger(LogoutHandler.class);

  private final StorageService storageService;

  public LogoutHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    String userId = request.headers().get(CustomHeaders.USER_ID.val());
    String token = request.headers().get(CustomHeaders.TOKEN.val());

    var s = storageService.getToken(userId);
    if (s.isEmpty()) {
      return Response.unauthorized();
    }

    if (!s.get().equals(token)) {
      return Response.unauthorized();
    }

    storageService.removeToken(userId);
    return Response.noContent();
  }
}
