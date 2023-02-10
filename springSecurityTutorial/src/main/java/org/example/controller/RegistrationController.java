package org.example.controller;

import org.example.dto.UserDto;
import org.example.dto.response.GenericResponse;
import org.example.enity.User;
import org.example.enity.VerificationToken;
import org.example.error.EmailExistsException;

import org.example.error.UserAlreadyExistException;
import org.example.event.OnRegistrationCompleteEvent;
import org.example.service.IUserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;


@Controller
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Logger logger;

    @Autowired
    Environment env;

    @GetMapping("/user/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/user/registration")
    public GenericResponse registerUserAccount(
            @Valid UserDto accountDto, HttpServletRequest request) throws UserAlreadyExistException, EmailExistsException {
        logger.debug("Registering user account with information: {}", accountDto);
        User registered = userService.registerNewUserAccount(accountDto);
        if (registered == null) {
            throw new UserAlreadyExistException();
        }
        String appUrl = "http://" + request.getServerName() + ":" +
                request.getServerPort() + request.getContextPath();

        eventPublisher.publishEvent(
                new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));

        return new GenericResponse("success");
    }
//    @PostMapping("/user/registration")
//    public ModelAndView registerUserAccount(
//            @ModelAttribute("user") @Valid UserDto userDto,
//            HttpServletRequest request,
//            Errors errors) {

//        try {
//            User registered = userService.registerNewUserAccount(userDto);
//
//            String appUrl = request.getContextPath();
//            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));
//        } catch (UserAlreadyExistException uaeEx) {
//            ModelAndView mav = new ModelAndView("registration", "user", userDto);
//            mav.addObject("message", "An account for that username/email already exists.");
//            return mav;
//        } catch (RuntimeException ex) {
//            return new ModelAndView("emailError", "user", userDto);
//        }
//
//        return new ModelAndView("successRegister", "user", userDto);
//    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(WebRequest request, Model model, @RequestParam("token") String token) {

        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "redirect:/badUser.html?lang=" + locale.getLanguage();
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
//            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("expired", true);
            model.addAttribute("message", messages.getMessage("auth.message.expired", null, locale));
            model.addAttribute("token", token);
            return "redirect:/badUser.html?lang=" + locale.getLanguage();
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        model.addAttribute("message", messages.getMessage("message.accountVerified", null, locale));
        return "redirect:/login.html?langu=" + locale.getLanguage();
    }

//    @GetMapping("/user/resendRegistrationToken")
//    public GenericResponse resendRegistrationToken ( HttpServletRequest request, @RequestParam("token") String existingToken) {
////        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
//
//        User user = userService.getUser(newToken.getToken());
//        String appUrl = "http://" + request.getServerName() + request.getServerPort() + request.getContextPath();
//
//        SimpleMailMessage email = constructResendVerificationTokenEmail (appUrl, request.getLocale(), newToken, user);
//        mailSender.send(email);
//
//        return new GenericResponse(
//                messages.getMessage("message.resendToken", null, request.getLocale())
//        );
//    }



//    non api
    private SimpleMailMessage constructResendVerificationTokenEmail (String contextPath, Locale locale, VerificationToken newToken, User user) {

        String confirmationUrl = contextPath + "/registrationConfirm.html?token=" + newToken.getToken();
        String message = messages.getMessage("message.resendToken", null, locale);
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Resend registration token");
        email.setText(message + " rn" + confirmationUrl);
        email.setFrom(env.getProperty("support.email"));
        email.setTo(user.getEmail());
        return email;
    }
}