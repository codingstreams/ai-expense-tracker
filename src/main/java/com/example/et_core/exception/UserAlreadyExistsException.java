package com.example.et_core.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String s) {
    super(s);
  }
}
