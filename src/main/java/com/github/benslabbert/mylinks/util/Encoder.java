package com.github.benslabbert.mylinks.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Encoder {

  private Encoder() {}

  public static String encode(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static byte[] encode(String string) {
    return string.getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] encodeUUID(UUID uuid) {
    return encode(uuid.toString());
  }

  public static UUID encodeUUID(byte[] bytes) {
    return UUID.fromString(encode(bytes));
  }

  public static UUID encodeUUID(String string) {
    return UUID.fromString(string);
  }
}
