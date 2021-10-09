package com.github.benslabbert.mylinks.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;

public record Response(HttpResponseStatus status, InputStream body) {

  private static final InputStream EMPTY_BODY = InputStream.nullInputStream();

  public static Response ok(InputStream body) {
    return new Response(HttpResponseStatus.OK, body);
  }

  public static Response created(InputStream body) {
    return new Response(HttpResponseStatus.CREATED, body);
  }

  public static Response badRequest() {
    return new Response(HttpResponseStatus.BAD_REQUEST, EMPTY_BODY);
  }

  public static Response badRequest(InputStream body) {
    return new Response(HttpResponseStatus.BAD_REQUEST, body);
  }

  public static Response conflict() {
    return new Response(HttpResponseStatus.CONFLICT, EMPTY_BODY);
  }

  public static Response unauthorized() {
    return new Response(HttpResponseStatus.UNAUTHORIZED, EMPTY_BODY);
  }

  public static Response methodNotAllowed() {
    return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED, EMPTY_BODY);
  }

  public static Response noContent() {
    return new Response(HttpResponseStatus.NO_CONTENT, EMPTY_BODY);
  }

  public static Response internalServerError() {
    return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR, EMPTY_BODY);
  }

  public static Response notFound() {
    return new Response(HttpResponseStatus.NOT_FOUND, EMPTY_BODY);
  }
}
