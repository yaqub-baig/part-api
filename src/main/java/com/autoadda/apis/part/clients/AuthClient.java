package com.autoadda.apis.part.clients;


import com.autoadda.apis.part.request.SmsRequest;
import com.autoadda.apis.part.response.GenericResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {

    @PostMapping("/auth/send-sms/{userId}")
    GenericResponse sendSms(@PathVariable Integer userId, @RequestBody SmsRequest request);
}
