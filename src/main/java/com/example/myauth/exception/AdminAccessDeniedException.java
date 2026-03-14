package com.example.myauth.exception;

public class AdminAccessDeniedException extends RuntimeException {
  public AdminAccessDeniedException(String message) {
    super(message);
  }
}
