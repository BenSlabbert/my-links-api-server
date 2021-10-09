package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.*;

import com.github.benslabbert.mylinks.service.StorageService;
import com.google.gson.Gson;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBusinessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpBusinessHandler.class);

  private final Map<String, RequestHandler> handlers;

  public HttpBusinessHandler(StorageService storageService, Gson gson) {
    this.handlers =
        Map.of(
            UriHandler.PATH, new UriHandler(storageService),
            CreateAccountHandler.PATH, new CreateAccountHandler(storageService),
            LogoutHandler.PATH, new LogoutHandler(storageService),
            LoginHandler.PATH, new LoginHandler(storageService, gson));
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) {
    if (!req.decoderResult().isSuccess()) {
      LOGGER.warn("failed to decode request with result: {}", req.decoderResult());
      ctx.close();
      return;
    }

    // when running with different threads we get the weird refcnt issues
    var resp = handleRequest(req);
    LOGGER.info("responding");
    var keepAlive = HttpUtil.isKeepAlive(req);
    LOGGER.info("keepAlive {}", keepAlive);

    var response = new DefaultHttpResponse(HTTP_1_1, resp.status());
    response.headers().set(TRANSFER_ENCODING, CHUNKED);

    if (keepAlive) {
      if (!req.protocolVersion().isKeepAliveDefault()) {
        response.headers().set(CONNECTION, KEEP_ALIVE);
      }
    } else {
      // Tell the client we're going to close the connection.
      response.headers().set(CONNECTION, CLOSE);
    }

    ctx.write(response);

    var f = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(resp.body())));
    if (!keepAlive) {
      LOGGER.info("close connection after write");
      f.addListener(ChannelFutureListener.CLOSE);
    }

    //        .exceptionallyAsync(
    //            throwable -> {
    //              if (throwable instanceof CompletionException ce) {
    //                if (ce.getCause() instanceof UnauthorizedException e) {
    //                  log.error("unauthorized exception", e);
    //                  return new Response(UNAUTHORIZED, nullInputStream());
    //                }
    //
    //                if (ce.getCause() instanceof ConflictException e) {
    //                  log.error("conflict exception", e);
    //                  return new Response(CONFLICT, nullInputStream());
    //                }
    //
    //                if (ce.getCause() instanceof BadRequestException e) {
    //                  log.error("bad request exception", e);
    //                  return new Response(BAD_REQUEST, nullInputStream());
    //                }
    //              }
    //
    //              log.error("exception handling request", throwable);
    //              return new Response(INTERNAL_SERVER_ERROR, nullInputStream());
    //            },
    //            executorGroup)
    //        .thenAcceptAsync(
    //            resp -> {
    //              log.info("responding");
    //              var keepAlive = HttpUtil.isKeepAlive(req);
    //              log.info("keepAlive {}", keepAlive);
    //
    //              var response = new DefaultHttpResponse(HTTP_1_1, resp.status());
    //              response.headers().set(TRANSFER_ENCODING, CHUNKED);
    //
    //              if (keepAlive) {
    //                if (!req.protocolVersion().isKeepAliveDefault()) {
    //                  response.headers().set(CONNECTION, KEEP_ALIVE);
    //                }
    //              } else {
    //                // Tell the client we're going to close the connection.
    //                response.headers().set(CONNECTION, CLOSE);
    //              }
    //
    //              ctx.write(response);
    //
    //              var f = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(resp.body())));
    //              if (!keepAlive) {
    //                log.info("close connection after write");
    //                f.addListener(ChannelFutureListener.CLOSE);
    //              }
    //            },
    //            executorGroup);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }

  private Response handleRequest(FullHttpRequest req) {
    var uri = URI.create(req.uri());
    var path = uri.getPath();

    if (!handlers.containsKey(path)) {
      return Response.notFound();
    }

    if (LoginHandler.PATH.equals(path) || CreateAccountHandler.PATH.equals(path)) {
      return handlers.get(path).handle(req);
    }

    for (var customHeader : CustomHeaders.values()) {
      if (customHeader.required() && StringUtils.isEmpty(req.headers().get(customHeader.val()))) {
        var str = "Required header: " + customHeader.val() + " not provided";
        var body = new ByteArrayInputStream(str.getBytes(UTF_8));
        return Response.badRequest(body);
      }
    }

    return handlers.get(path).handle(req);
  }
}
