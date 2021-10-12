package com.github.benslabbert.mylinks.dto;

import com.google.gson.reflect.TypeToken;
import java.util.Objects;
import java.util.UUID;

// leave as class for gson
public final class LoginResponseDto {

  public static final TypeToken<LoginResponseDto> TYPE_TOKEN = new TypeToken<>() {};
  private final UUID userId;
  private final UUID token;

  public LoginResponseDto(UUID userId, UUID token) {
    this.userId = userId;
    this.token = token;
  }

  public UUID userId() {
    return userId;
  }

  public UUID token() {
    return token;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (LoginResponseDto) obj;
    return Objects.equals(this.userId, that.userId) && Objects.equals(this.token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, token);
  }

  @Override
  public String toString() {
    return "LoginResponseDto[" + "userId=" + userId + ", " + "token=" + token + ']';
  }
}
