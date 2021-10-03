package com.github.benslabbert.mylinks.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.concurrent.CompletableFuture.completedFuture;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBusinessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger log = LoggerFactory.getLogger(HttpBusinessHandler.class);

  private final Map<String, RequestHandler> handlers;

  public HttpBusinessHandler() {
    handlers = Map.of("/uris", new UriHandler());
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
        .thenApply(
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

              var f = ctx.write(new HttpChunkedInput(new ChunkedStream(resp.body())));
              if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
              }

              return completedFuture(null);
            });
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
      InputStream body = new ByteArrayInputStream("not found".getBytes(StandardCharsets.UTF_8));
      return completedFuture(new Response(HttpResponseStatus.NOT_FOUND, body));
    }

    if (StringUtils.isEmpty(req.headers().get(CustomHeaders.REQ_ID))) {
      InputStream body =
          new ByteArrayInputStream("Required header not provided".getBytes(StandardCharsets.UTF_8));
      return completedFuture(new Response(HttpResponseStatus.BAD_REQUEST, body));
    }

    return handlers.get(path).handle(req);
  }
}
