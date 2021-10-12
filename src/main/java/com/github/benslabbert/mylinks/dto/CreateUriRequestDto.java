package com.github.benslabbert.mylinks.dto;

import com.google.gson.reflect.TypeToken;
import java.util.Objects;

// leave as class for gson
public final class CreateUriRequestDto {

  public static final TypeToken<CreateUriRequestDto> TYPE_TOKEN = new TypeToken<>() {};
  private final String uri;

  public CreateUriRequestDto(String uri) {
    this.uri = uri;
  }

  public String uri() {
    return uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (CreateUriRequestDto) obj;
    return Objects.equals(this.uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return "CreateUriRequestDto[" + "uri=" + uri + ']';
  }
}
