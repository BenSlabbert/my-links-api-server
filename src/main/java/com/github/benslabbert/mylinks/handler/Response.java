package com.github.benslabbert.mylinks.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;

public record Response(HttpResponseStatus status, InputStream body) {}
