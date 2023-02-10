package org.example.enity;

import lombok.Data;
import org.example.validation.ValidEmail;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;

    private String lastName;

    private String password;
    private String matchingPassword;

    private String email;

    private String role;
}
