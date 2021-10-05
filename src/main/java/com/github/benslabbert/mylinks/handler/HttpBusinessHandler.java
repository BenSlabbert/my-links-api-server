package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.io.InputStream.nullInputStream;
import static java.nio.charset.StandardCharsets.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.github.benslabbert.mylinks.exception.ConflictException;
import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class HttpBusinessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger log = LoggerFactory.getLogger(HttpBusinessHandler.class);

  private final Map<String, RequestHandler> handlers;
  private final DefaultEventExecutorGroup group;

  public HttpBusinessHandler(DefaultEventExecutorGroup group, JedisPool jedisPool) {
    this.group = group;
    this.handlers =
        Map.of(
            UriHandler.PATH, new UriHandler(jedisPool),
            CreateAccountHandler.PATH, new CreateAccountHandler(jedisPool),
            LogoutHandler.PATH, new LogoutHandler(jedisPool),
            LoginHandler.PATH, new LoginHandler(jedisPool));
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
    if (!req.decoderResult().isSuccess()) {
      log.warn("failed to decode request with result: {}", req.decoderResult());
      ctx.close();
      return;
    }

    handleRequest(req)
        .exceptionallyAsync(
            throwable -> {
              if (throwable instanceof CompletionException ce) {
                if (ce.getCause() instanceof UnauthorizedException e) {
                  log.error("unauthorized exception", e);
                  return new Response(UNAUTHORIZED, nullInputStream());
                }

                if (ce.getCause() instanceof ConflictException e) {
                  log.error("conflict exception", e);
                  return new Response(CONFLICT, nullInputStream());
                }
              }

              log.error("exception handling request", throwable);
              return new Response(INTERNAL_SERVER_ERROR, nullInputStream());
            },
            group)
        .thenAcceptAsync(
            resp -> {
              log.info("handling resp");
              var keepAlive = HttpUtil.isKeepAlive(req);
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
                f.addListener(ChannelFutureListener.CLOSE);
              }
            },
            group);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }

  private CompletableFuture<Response> handleRequest(FullHttpRequest req) {
    var uri = URI.create(req.uri());
    var path = uri.getPath();

    if (!handlers.containsKey(path)) {
      InputStream body = new ByteArrayInputStream("not found".getBytes(UTF_8));
      return completedFuture(new Response(NOT_FOUND, body));
    }

    if ("/auth".equals(path)) {
      return supplyAsync(() -> handlers.get(path).handle(req), group);
    }

    for (var customHeader : CustomHeaders.values()) {
      if (customHeader.required() && StringUtils.isEmpty(req.headers().get(customHeader.val()))) {
        var str = "Required header: " + customHeader.val() + " not provided";
        var body = new ByteArrayInputStream(str.getBytes(UTF_8));
        return completedFuture(new Response(BAD_REQUEST, body));
      }
    }

    return supplyAsync(() -> handlers.get(path).handle(req), group);
  }
}
