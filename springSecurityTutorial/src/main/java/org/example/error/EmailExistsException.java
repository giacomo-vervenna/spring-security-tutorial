package org.example.error;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class EmailExistsException extends Exception{

    public EmailExistsException(String s){
        super();
    }

}
