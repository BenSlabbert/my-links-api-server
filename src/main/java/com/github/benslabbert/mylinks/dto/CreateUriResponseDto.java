package com.github.benslabbert.mylinks.dto;

import com.google.gson.reflect.TypeToken;
import java.util.Objects;
import java.util.UUID;

// leave as class for gson
public final class CreateUriResponseDto {

  public static final TypeToken<CreateUriResponseDto> TYPE_TOKEN = new TypeToken<>() {};
  private final UUID id;

  public CreateUriResponseDto(UUID id) {
    this.id = id;
  }

  public UUID id() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (CreateUriResponseDto) obj;
    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "CreateUriResponseDto[" + "id=" + id + ']';
  }
}
