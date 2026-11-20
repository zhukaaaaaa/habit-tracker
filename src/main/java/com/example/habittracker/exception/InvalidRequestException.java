package com.example.habittracker.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) { super(message); }
}
