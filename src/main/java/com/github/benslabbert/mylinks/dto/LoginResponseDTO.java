package com.github.benslabbert.mylinks.dto;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.UUID;

public record LoginResponseDTO(UUID userId, UUID token) {

  public static Type TYPE = new TypeToken<LoginResponseDTO>() {}.getType();
}
