package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.service.StorageService;
import com.github.benslabbert.mylinks.util.RequestUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
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
    if (!request.method().name().equals("POST")) {
      return Response.methodNotAllowed();
    }

    var bytes = RequestUtil.readRequestBodyFully(request);
    if (bytes.length == 0) {
      return Response.badRequest();
    }

    var body = new String(bytes, StandardCharsets.UTF_8);

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
