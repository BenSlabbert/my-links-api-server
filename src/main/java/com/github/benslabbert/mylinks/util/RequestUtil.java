package com.github.benslabbert.mylinks.util;

import static com.github.benslabbert.mylinks.util.Encoder.encode;

import com.github.benslabbert.mylinks.exception.BadRequestException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBufHolder;

public class RequestUtil {

  private static final Gson GSON = new Gson();

  private RequestUtil() {}

  public static byte[] readRequestBodyFully(ByteBufHolder byteBufHolder) {
    var byteBuf = byteBufHolder.content().copy();
    int readableBytes = byteBuf.readableBytes();

    var bytes = new byte[readableBytes];
    byteBuf.readBytes(bytes);
    byteBuf.release();

    return bytes;
  }

  public static <T> T readBodyAs(ByteBufHolder byteBufHolder, TypeToken<T> typeToken) {
    var bytes = readRequestBodyFully(byteBufHolder);

    if (bytes.length == 0) {
      throw new BadRequestException("no content in request");
    }

    return GSON.fromJson(encode(bytes), typeToken.getType());
  }
}
