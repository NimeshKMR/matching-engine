package com.nimeshkmr.core;

public class DuplicateOrderIDException extends RuntimeException{
    
    public DuplicateOrderIDException(String message){
        super(message);
    }
}
