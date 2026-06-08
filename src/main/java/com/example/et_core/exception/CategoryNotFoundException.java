package com.example.et_core.exception;

public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException(Long accountId) {
        super("Category not found with id " + accountId);
    }
}
