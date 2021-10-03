package com.github.benslabbert.mylinks.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import java.util.concurrent.CompletableFuture;

public interface RequestHandler {

  CompletableFuture<Response> handle(CompletableFuture<FullHttpRequest> future);
}
