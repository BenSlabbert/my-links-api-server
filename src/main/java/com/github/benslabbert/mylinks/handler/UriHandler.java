package com.github.benslabbert.mylinks.handler;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriHandler implements RequestHandler {

  private static final Logger log = LoggerFactory.getLogger(UriHandler.class);

  private static final Response BAD_REQUEST =
      new Response(HttpResponseStatus.BAD_REQUEST, InputStream.nullInputStream());

  @Override
  public CompletableFuture<Response> handle(FullHttpRequest req) {
    return switch (req.method().name()) {
      case "GET" -> get(req);
      case "POST" -> post(req);
      default -> completedFuture(BAD_REQUEST);
    };
  }

  private CompletableFuture<Response> get(FullHttpRequest req) {
    // todo here we will go to redis and get the response

    var reqId = req.headers().get(CustomHeaders.REQ_ID);
    log.info("{} handle get", reqId);

    var data = RandomStringUtils.randomAlphabetic(1024).getBytes(StandardCharsets.UTF_8);
    return completedFuture(new Response(HttpResponseStatus.OK, new ByteArrayInputStream(data)));
  }

  private CompletableFuture<Response> post(FullHttpRequest req) {
    // todo here we will write to redis

    var reqId = req.headers().get(CustomHeaders.REQ_ID);
    log.info("{} handle post", reqId);

    var content = req.content();
    var bodyStr = content.toString(StandardCharsets.UTF_8);
    content.release();

    log.info("body {}", bodyStr);
    return completedFuture(new Response(HttpResponseStatus.OK, InputStream.nullInputStream()));
  }
}
