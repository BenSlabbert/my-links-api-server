package com.github.benslabbert.mylinks.handler;

import static com.github.benslabbert.mylinks.handler.CustomHeaders.REQ_ID;
import static com.github.benslabbert.mylinks.handler.CustomHeaders.USER_ID;
import static com.github.benslabbert.mylinks.util.Encoder.encodeUUID;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.benslabbert.mylinks.aop.Secured;
import com.github.benslabbert.mylinks.dto.CreateUriRequestDto;
import com.github.benslabbert.mylinks.dto.CreateUriResponseDto;
import com.github.benslabbert.mylinks.dto.GetUrisResponseDto;
import com.github.benslabbert.mylinks.service.StorageService;
import com.github.benslabbert.mylinks.util.RequestUtil;
import com.google.gson.Gson;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriHandler implements RequestHandler {

  public static final String PATH = "/uris";

  private static final Logger LOGGER = LoggerFactory.getLogger(UriHandler.class);

  private final StorageService storageService;
  private final Gson gson;

  public UriHandler(StorageService storageService, Gson gson) {
    this.storageService = storageService;
    this.gson = gson;
  }

  @Secured
  @Override
  public Response handle(FullHttpRequest request) {
    return switch (request.method().name()) {
      case "GET" -> get(request);
      case "POST" -> post(request);
      default -> Response.methodNotAllowed();
    };
  }

  private Response get(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    LOGGER.info("{} handle get", reqId);

    var userId = encodeUUID(request.headers().get(USER_ID.val()));
    var uris = storageService.getUris(userId);

    var json = gson.toJson(new GetUrisResponseDto(uris), GetUrisResponseDto.TYPE_TOKEN.getType());

    return Response.ok(
        Map.of(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON),
        IOUtils.toInputStream(json, UTF_8));
  }

  private Response post(FullHttpRequest request) {
    var reqId = request.headers().get(REQ_ID.val());
    LOGGER.info("{} handle post", reqId);

    var userId = encodeUUID(request.headers().get(USER_ID.val()));
    var createUriReq = RequestUtil.readBodyAs(request, CreateUriRequestDto.TYPE_TOKEN);
    var entryId = storageService.addUri(userId, createUriReq.uri());

    var json =
        gson.toJson(new CreateUriResponseDto(entryId), CreateUriResponseDto.TYPE_TOKEN.getType());

    return Response.created(
        Map.of(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON),
        IOUtils.toInputStream(json, UTF_8));
  }
}
