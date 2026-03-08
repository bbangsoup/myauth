package com.example.myauth.exception;

public class DmAccessDeniedException extends RuntimeException {

  public DmAccessDeniedException(String message) {
    super(message);
  }

  public DmAccessDeniedException() {
    super("해당 DM 방에 접근할 권한이 없습니다.");
  }
}
