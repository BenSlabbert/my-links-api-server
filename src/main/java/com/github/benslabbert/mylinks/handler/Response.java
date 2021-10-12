package com.github.benslabbert.mylinks.handler;

import com.github.benslabbert.mylinks.exception.BadRequestException;
import com.github.benslabbert.mylinks.exception.ConflictException;
import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import java.io.InputStream;
import java.util.Map;

public record Response(
    HttpResponseStatus status, Map<AsciiString, AsciiString> headers, InputStream body) {

  public Response(HttpResponseStatus status, InputStream body) {
    this(status, Map.of(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN), body);
  }

  public Response(HttpResponseStatus status) {
    this(status, InputStream.nullInputStream());
  }

  public static Response ok(InputStream body) {
    return new Response(HttpResponseStatus.OK, body);
  }

  public static Response ok(Map<AsciiString, AsciiString> headers, InputStream body) {
    return new Response(HttpResponseStatus.OK, headers, body);
  }

  public static Response created(InputStream body) {
    return new Response(HttpResponseStatus.CREATED, body);
  }

  public static Response created(Map<AsciiString, AsciiString> headers, InputStream body) {
    return new Response(HttpResponseStatus.CREATED, headers, body);
  }

  public static Response badRequest() {
    return new Response(HttpResponseStatus.BAD_REQUEST);
  }

  public static Response badRequest(InputStream body) {
    return new Response(HttpResponseStatus.BAD_REQUEST, body);
  }

  public static Response conflict() {
    return new Response(HttpResponseStatus.CONFLICT);
  }

  public static Response unauthorized() {
    return new Response(HttpResponseStatus.UNAUTHORIZED);
  }

  public static Response methodNotAllowed() {
    return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
  }

  public static Response noContent() {
    return new Response(HttpResponseStatus.NO_CONTENT);
  }

  public static Response internalServerError() {
    return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR);
  }

  public static Response notFound() {
    return new Response(HttpResponseStatus.NOT_FOUND);
  }

  public static Response withException(Exception exception) {

    if (exception instanceof BadRequestException) {
      return badRequest();
    }

    if (exception instanceof ConflictException) {
      return conflict();
    }

    if (exception instanceof UnauthorizedException) {
      return unauthorized();
    }

    return internalServerError();
  }
}
