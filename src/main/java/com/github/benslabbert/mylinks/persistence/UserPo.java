package com.github.benslabbert.mylinks.persistence;

import static com.github.benslabbert.mylinks.util.Encoder.encode;
import static com.github.benslabbert.mylinks.util.Encoder.encodeUUID;

import java.util.Map;
import java.util.UUID;

public record UserPo(UUID id, String username, String password) implements PersistedObject {

  public static final byte[] FIELD_ID = encode("id");
  public static final byte[] FIELD_USERNAME = encode("username");
  public static final byte[] FIELD_PASSWORD = encode("password");

  public static UserPo fromHmap(Map<byte[], byte[]> hmap) {
    return new UserPo(
        encodeUUID(hmap.get(FIELD_ID)),
        encode(hmap.get(FIELD_USERNAME)),
        encode(hmap.get(FIELD_PASSWORD)));
  }

  @Override
  public Map<byte[], byte[]> serialize() {
    return Map.of(
        FIELD_ID, encodeUUID(id),
        FIELD_USERNAME, encode(username),
        FIELD_PASSWORD, encode(password));
  }
}
