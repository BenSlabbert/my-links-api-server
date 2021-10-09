package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateAccountHandler implements RequestHandler {

  public static final String PATH = "/create_account";

  private static final Logger log = LoggerFactory.getLogger(CreateAccountHandler.class);

  private final StorageService storageService;

  public CreateAccountHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    log.info("handle request");

    if (!request.method().name().equals("POST")) {
      return Response.methodNotAllowed();
    }

    ByteBuf byteBuf = request.content().copy();
    int readableBytes = byteBuf.readableBytes();
    log.info("readableBytes {}", readableBytes);

    var bytes = new byte[readableBytes];
    byteBuf.readBytes(bytes);
    boolean refContEqualsZero = byteBuf.release();
    log.info("refContEqualsZero {}", refContEqualsZero);

    var body = new String(bytes, StandardCharsets.UTF_8);

    if (StringUtils.isEmpty(body)) {
      return Response.badRequest();
    }

    log.info("body: {}", body);

    var arr = body.split("&");
    if (arr.length != 2) {
      return Response.badRequest();
    }

    var username = arr[0].split("=")[1];
    var password = arr[1].split("=")[1];

    var s = storageService.getUser(username);

    if (s.isPresent()) {
      return Response.conflict();
    }

    storageService.setUser(username, password);
    var token = UUID.randomUUID();
    storageService.setToken(username, token);

    return Response.created(
        new ByteArrayInputStream(token.toString().getBytes(StandardCharsets.UTF_8)));
  }
}
