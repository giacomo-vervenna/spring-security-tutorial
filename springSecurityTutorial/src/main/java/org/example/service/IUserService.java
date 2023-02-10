package org.example.service;

import org.example.dto.UserDto;
import org.example.enity.User;
import org.example.enity.VerificationToken;
import org.example.error.EmailExistsException;

public interface IUserService {

    User registerNewUserAccount(UserDto userDto) throws EmailExistsException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    VerificationToken getVerificationToken(String verificationToken);

//    VerificationToken generateNewVerificationToken(String existingToken);
}
