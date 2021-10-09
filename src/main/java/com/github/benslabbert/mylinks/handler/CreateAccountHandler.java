package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateAccountHandler implements RequestHandler {

  public static final String PATH = "/create_account";

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateAccountHandler.class);

  private final StorageService storageService;

  public CreateAccountHandler(StorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    LOGGER.info("handle request");

    if (!request.method().name().equals("POST")) {
      return Response.methodNotAllowed();
    }

    ByteBuf byteBuf = request.content().copy();
    int readableBytes = byteBuf.readableBytes();
    LOGGER.info("readableBytes {}", readableBytes);

    var bytes = new byte[readableBytes];
    byteBuf.readBytes(bytes);
    boolean refContEqualsZero = byteBuf.release();
    LOGGER.info("refContEqualsZero {}", refContEqualsZero);

    var body = new String(bytes, StandardCharsets.UTF_8);

    if (StringUtils.isEmpty(body)) {
      return Response.badRequest();
    }

    LOGGER.info("body: {}", body);

    var arr = body.split("&");
    if (arr.length != 2) {
      return Response.badRequest();
    }

    var username = arr[0].split("=")[1];
    var password = arr[1].split("=")[1];

    if (storageService.getUser(username).isPresent()) {
      return Response.conflict();
    }

    var user = storageService.createUser(username, password);
    var token = UUID.randomUUID();
    storageService.setToken(user.id(), token);

    return Response.created(IOUtils.toInputStream(token.toString(), StandardCharsets.UTF_8));
  }
}
