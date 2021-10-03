package com.github.benslabbert.mylinks.handler;

public enum CustomHeaders {
  REQ_ID("X-ReqId"),
  USER_ID("X-UserId"),
  TOKEN("X-Token");

  private final String val;

  CustomHeaders(String val) {
    this.val = val;
  }

  public String val() {
    return val;
  }
}
