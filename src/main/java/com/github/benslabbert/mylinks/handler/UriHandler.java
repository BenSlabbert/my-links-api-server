package com.github.benslabbert.mylinks.handler;

import static com.github.benslabbert.mylinks.handler.CustomHeaders.REQ_ID;
import static com.github.benslabbert.mylinks.handler.CustomHeaders.TOKEN;
import static com.github.benslabbert.mylinks.handler.CustomHeaders.USER_ID;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriHandler implements RequestHandler {

  public static final String PATH = "/uris";

  private static final Logger log = LoggerFactory.getLogger(UriHandler.class);

  private final StorageService storageService;

  public UriHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");
    var userId = request.headers().get(USER_ID.val());
    var token = request.headers().get(TOKEN.val());

    // this is an authenticated request
    var storedToken = storageService.getToken(userId);
    if (storedToken.isEmpty()) {
      return Response.unauthorized();
    }

    if (!storedToken.get().equals(token)) {
      return Response.unauthorized();
    }

    return switch (request.method().name()) {
      case "GET" -> get(request);
      case "POST" -> post(request);
      default -> Response.methodNotAllowed();
    };
  }

  private Response get(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    log.info("{} handle get", reqId);

    var data = RandomStringUtils.randomAlphabetic(1024).getBytes(UTF_8);
    return Response.ok(new ByteArrayInputStream(data));
  }

  private Response post(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    log.info("{} handle post", reqId);

    var content = request.content();
    var bodyStr = content.toString(UTF_8);
    content.release();

    log.info("body {}", bodyStr);
    return Response.noContent();
  }
}
