package com.example.studenthelper;

/**
 * Created by DELL on 3/25/2018.
 */

public class ExceptionHandler extends Exception {
    private String message;

    public ExceptionHandler(){
        message = "Exception Occurred";
    }

    public ExceptionHandler(String m){
        message = m;
    }

    public String getMessage(){
        return message;
    }
}
