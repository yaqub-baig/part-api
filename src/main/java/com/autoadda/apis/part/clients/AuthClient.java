package com.autoadda.apis.part.clients;


import com.autoadda.apis.part.request.EmailRequest;
import com.autoadda.apis.part.request.SendNotificationRequest;
import com.autoadda.apis.part.request.SmsRequest;
import com.autoadda.apis.part.response.CommunicationPreference;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {

    @PostMapping("/auth/send-sms/{userId}")
    GenericResponse sendSms(@PathVariable Integer userId, @RequestBody SmsRequest request);



    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);

    @GetMapping("/preferences/isc/{customerId}")
    ResponseEntity<CommunicationPreference> getPreferencesByCustomerId1(
            @PathVariable Integer customerId);

    @PostMapping("/auth/send-sms")
    GenericResponse sendSms(@RequestBody SmsRequest request);

    @PostMapping("/auth/send-email/{userId}")
    GenericResponse sendEmail(@PathVariable Integer userId,@RequestBody EmailRequest request );

    @PostMapping("/notifications/send")
    ResponseEntity<Map<String, Object>> sendNotification(@RequestBody SendNotificationRequest request);
}
