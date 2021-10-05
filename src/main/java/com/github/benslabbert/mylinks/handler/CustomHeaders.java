package com.github.benslabbert.mylinks.handler;

public enum CustomHeaders {
  REQ_ID("X-ReqId", true),
  USER_ID("X-UserId", true),
  TOKEN("X-Token", true);

  private final String val;
  private final boolean required;

  CustomHeaders(String val, boolean required) {
    this.val = val;
    this.required = required;
  }

  public String val() {
    return val;
  }

  public boolean required() {
    return required;
  }
}
