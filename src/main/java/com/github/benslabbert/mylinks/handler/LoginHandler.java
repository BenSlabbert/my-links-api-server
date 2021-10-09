package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.dto.LoginResponseDTO;
import com.github.benslabbert.mylinks.service.StorageService;
import com.github.benslabbert.mylinks.util.BasicAuthUtil;
import com.google.gson.Gson;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler implements RequestHandler {

  public static final String PATH = "/login";

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);

  private final StorageService storageService;
  private final Gson gson;

  public LoginHandler(StorageService storageService, Gson gson) {
    this.storageService = storageService;
    this.gson = gson;
  }

  @Override
  public Response handle(FullHttpRequest request) {
    LOGGER.info("handle request");

    var credentials = BasicAuthUtil.getCredentials(request);

    var user = storageService.getUser(credentials.username());

    if (user.isEmpty()) {
      return Response.unauthorized();
    }

    if (!user.get().password().equals(credentials.password())) {
      return Response.unauthorized();
    }

    var token = UUID.randomUUID();
    storageService.setToken(user.get().id(), token);

    var json = gson.toJson(new LoginResponseDTO(user.get().id(), token), LoginResponseDTO.TYPE);

    return Response.ok(
        Map.of(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON),
        IOUtils.toInputStream(json, StandardCharsets.UTF_8));
  }
}
