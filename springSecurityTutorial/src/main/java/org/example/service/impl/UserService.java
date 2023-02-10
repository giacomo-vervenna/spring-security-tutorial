package org.example.service.impl;

import org.example.dto.UserDto;
import org.example.enity.Role;
import org.example.enity.User;
import org.example.enity.VerificationToken;
import org.example.error.EmailExistsException;
import org.example.repository.UserRepository;
import org.example.repository.VerificationTokenRepository;
import org.example.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class UserService implements IUserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Override
    public User registerNewUserAccount(UserDto userDto) throws EmailExistsException {
        if (emailExists(userDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRoles((List<Role>) new Role(Integer.valueOf(1), user));
        return repository.save(user);
    }

    @Override
    public User getUser(String verificationToken) {
        User user = tokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    @Override
    public void saveRegisteredUser(User user) {
       repository.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(user, token);
        tokenRepository.save(myToken);

    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }

//    @Override
//    public VerificationToken generateNewVerificationToken(final String existingVerificationToken) {
//        VerificationToken vToken = tokenRepository.findByToken(existingVerificationToken);
//        vToken.updateToken(UUID.randomUUID()
//                .toString());
//        vToken = tokenRepository.save(vToken);
//        return vToken;
//    }

    private boolean emailExists(String email) {
        return repository.findByEmail(email) != null;
    }
}