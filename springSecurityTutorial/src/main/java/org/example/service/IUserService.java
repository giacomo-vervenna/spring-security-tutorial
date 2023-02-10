package org.example.service;

import org.example.dto.UserDto;
import org.example.enity.User;

public interface IUserService {

    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;
}
