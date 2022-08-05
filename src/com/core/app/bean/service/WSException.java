/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.bean.service;

/**
 *
 * @author ergarcia
 */
public class WSException extends Throwable{

    public WSException(String message) {
        super(message);
    }

    public WSException() {
    }

    public WSException(Throwable cause) {
        super(cause);
    }

    public WSException(String message, Throwable cause) {
        super(message, cause);
    }
        
    
}
