package com.example.et_core.exception;

public class UserAlreadyOnboardedException extends RuntimeException {
    public UserAlreadyOnboardedException(String userId) {
        super("User with id " + userId + " is already onboarded.");
    }
}
