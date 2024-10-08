package com.app.mederrand.utils.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.mederrand.utils.services.EmailSenderService;

public class SendOtpWorker extends Worker {

    public SendOtpWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userEmail = getInputData().getString("userEmail");
        String userFullName = getInputData().getString("userFullName");
        String otpCode = getInputData().getString("otpCode");

        String subject = "Mederrand - Email Verification OTP";
        String body = "Dear " + userFullName + ",<br><br>" +
                "Thank you for choosing Mederrand App! To complete your registration and ensure the security of your account, please use the following OTP code to verify your email address: <br>" +
                "<b>Your OTP Code:</b> " + otpCode + "<br>" +
                "If you didn't request this verification, please ignore this email.<br><br>" +
                "Thank you for being a part of Mederrand APP!<br><br>" +
                "Best regards,<br>" +
                "The Mederrand Team";

        EmailSenderService.sendOtp(userEmail, subject, body);
        return Result.success();
    }
}
