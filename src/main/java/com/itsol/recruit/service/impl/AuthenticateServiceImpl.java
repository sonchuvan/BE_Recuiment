package com.itsol.recruit.service.impl;

import com.itsol.recruit.core.Constants;
import com.itsol.recruit.dto.ResponseDTO;
import com.itsol.recruit.dto.UserDTO;
import com.itsol.recruit.entity.OTP;
import com.itsol.recruit.entity.Role;
import com.itsol.recruit.entity.User;
import com.itsol.recruit.repository.AuthenticateRepository;
import com.itsol.recruit.repository.OTPRepository;
import com.itsol.recruit.repository.RoleRepository;
import com.itsol.recruit.repository.UserRepository;
import com.itsol.recruit.security.jwt.TokenProvider;
import com.itsol.recruit.service.AuthenticateService;
import com.itsol.recruit.service.email.EmailService;
import com.itsol.recruit.service.mapper.UserMapper;
import com.itsol.recruit.web.vm.ChangePassVM;
import com.itsol.recruit.web.vm.LoginVM;
import com.sun.xml.internal.ws.handler.HandlerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class AuthenticateServiceImpl implements AuthenticateService {

    public final AuthenticateRepository authenticateRepository;

    public final UserMapper userMapper;

    public final RoleRepository roleRepository;

    public final UserRepository userRepository;

    public final OTPRepository otpRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final TokenProvider tokenProvider;

    public final String linkActiveAccount = "http://localhost:4200/active?code=";

    public AuthenticateServiceImpl(AuthenticateRepository authenticateRepository, UserMapper userMapper, RoleRepository roleRepository, UserRepository userRepository, OTPRepository otpRepository, PasswordEncoder passwordEncoder, EmailService emailService, AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider) {
        this.authenticateRepository = authenticateRepository;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ResponseDTO signup(UserDTO dto) {

        User userName = userRepository.findByUserName(dto.getUserName());
        User email = userRepository.findUserByEmail(dto.getEmail());

        if (userName != null) throw new NullPointerException();
        if (email != null) throw new HandlerException("email");

        Set<Role> roles = roleRepository.findByCode(Constants.Role.USER);
        User user = userMapper.toEntity(dto);
        user.setDelete(false);
        user.setActive(false);
        user.setActive(false);
        user.setDelete(false);
        user.setRoles(roles);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String enCryptPassword = bCryptPasswordEncoder.encode(dto.getPassword());
        user.setPassword(enCryptPassword);

        userRepository.save(user);

        OTP otp = new OTP(user);
        otpRepository.save(otp);
        String link = linkActiveAccount + otp.getCode();
        String emails = emailService.buildActiveLink(link);
        emailService.sendEmail(user.getEmail(), emails);
        return new ResponseDTO("Signup success");

//        OTP otp = userService.generateOTP(user);
//        String linkActive = accountActivationConfig.getActivateUrl() + user.getId();
//        emailService.sendSimpleMessage(user.getEmail(),
//                "Link active account",
//                "<a href=\" " + linkActive + "\">Click vào đây để kích hoạt tài khoản</a>");


    }

    @Override
    public String changePassword(ChangePassVM changePassVM) {
        String message;
        User user = userRepository.findUserByEmail(changePassVM.getEmail());
        if (user != null) {
            OTP optdb = otpRepository.findByUser(user);
            if (optdb.isExpired())
                message = "expired";
            else if (optdb.getCode().equals(changePassVM.getCode())) {
                user.setPassword(passwordEncoder.encode(changePassVM.getPassword()));
                userRepository.save(user);
                message = "success";
            } else {
                message = "notfound";
            }
        } else {
            message = "fail";
        }
        return message;


    }
}
