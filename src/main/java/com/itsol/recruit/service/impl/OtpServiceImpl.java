package com.itsol.recruit.service.impl;

import com.itsol.recruit.dto.ResponseDTO;
import com.itsol.recruit.entity.OTP;
import com.itsol.recruit.entity.User;
import com.itsol.recruit.repository.OTPRepository;
import com.itsol.recruit.repository.UserRepository;
import com.itsol.recruit.service.OtpService;
import com.itsol.recruit.service.email.EmailService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class OtpServiceImpl implements OtpService {
    private final UserRepository userRepository;

    private final OTPRepository otpRepository;

    private final EmailService emailService;

    public OtpServiceImpl(UserRepository userRepository, OTPRepository otpRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }
    @Override
    public ResponseDTO sendOTP(String email) {
        User user=userRepository.findUserByEmail(email);
        if(user==null) throw new UsernameNotFoundException("");
        OTP otp=new OTP(user);
        OTP oldOTP=otpRepository.findByUser(user);
        if(oldOTP!=null){
            oldOTP.setCode(otp.getCode());
            oldOTP.setIssueAt(otp.getIssueAt());
            otpRepository.save(oldOTP);
        }
        else{
            otpRepository.save(otp);
        }

        String emails=emailService.buildOtpEmail(user.getUserName(), otp.getCode());
        emailService.sendEmail(user.getEmail(),emails);
        return new ResponseDTO("Send sucess");
    }
}
