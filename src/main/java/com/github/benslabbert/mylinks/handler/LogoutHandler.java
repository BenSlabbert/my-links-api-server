package com.github.benslabbert.mylinks.handler;

import static com.github.benslabbert.mylinks.util.Encoder.encodeUUID;

import com.github.benslabbert.mylinks.aop.Secured;
import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutHandler implements RequestHandler {

  public static final String PATH = "/logout";

  private static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);

  private final StorageService storageService;

  public LogoutHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Secured
  @Override
  public Response handle(FullHttpRequest request) {
    var userId = encodeUUID(request.headers().get(CustomHeaders.USER_ID.val()));
    storageService.removeToken(userId);
    return Response.noContent();
  }
}
