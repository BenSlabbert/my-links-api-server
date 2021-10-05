package com.github.benslabbert.mylinks.handler;

import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestHandler {

  Response handle(FullHttpRequest future);
}
