package com.anios.ipification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "extractToken", url = "${feign.token.url}")
public interface IpificationFeign {

    @PostMapping(value = "/auth/realms/ipification/protocol/openid-connect/token")
    ResponseEntity<?> fetchToken(@RequestBody MultiValueMap<String, String> formData);

    @GetMapping("/auth/realms/ipification/protocol/openid-connect/userinfo")
    ResponseEntity<?> userDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);
}
