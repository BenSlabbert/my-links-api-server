package com.github.benslabbert.mylinks.dto;

import com.google.gson.reflect.TypeToken;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// leave as class for gson
public final class GetUrisResponseDto {

  public static final TypeToken<GetUrisResponseDto> TYPE_TOKEN = new TypeToken<>() {};
  private final Map<UUID, String> uris;

  public GetUrisResponseDto(Map<UUID, String> uris) {
    this.uris = uris;
  }

  public Map<UUID, String> uris() {
    return uris;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (GetUrisResponseDto) obj;
    return Objects.equals(this.uris, that.uris);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uris);
  }

  @Override
  public String toString() {
    return "GetUrisResponseDto[" + "uris=" + uris + ']';
  }
}
