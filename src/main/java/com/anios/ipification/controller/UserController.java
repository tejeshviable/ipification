package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.MobileRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class UserController {

    @Autowired
    UserService userService;

    /*@PostMapping("/generateUrl")
    public ResponseEntity<?> generateUrl(@RequestBody MobileRequestDTO mobileRequestDTO){
        return new ResponseEntity<>(userService.generateUrl(mobileRequestDTO), HttpStatus.MOVED_TEMPORARILY);
    }
    
     */
    /*@PostMapping("/generateUrl")
    public ResponseEntity<?> generateUrl(@RequestBody MobileRequestDTO mobileRequestDTO){
        return new ResponseEntity<>(userService.generateUrl(mobileRequestDTO), HttpStatus.MOVED_TEMPORARILY);
    }
*/
    @GetMapping("/callback1")
    @ResponseBody
    public ResponseEntity<?> handleRedirect(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error) {

        // Log the received parameters
        System.out.println("Received code: " + code);
        System.out.println("Received state: " + state);
        return new ResponseEntity<>(userService.saveVerificationStatus(code, null, null), HttpStatus.OK);
    }

    @GetMapping("/callback")
    @ResponseBody
    public ResponseEntity<?> handleRedirect(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) {

        // Log the received parameters
        System.out.println("Received code: " + code);
        System.out.println("Received state: " + state);
        System.out.println("Received error: " + error);
        System.out.println("Received error_description: " + errorDescription);
        return new ResponseEntity<>(userService.saveVerificationStatus(code, error, errorDescription), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{mobileNumber}")
    public ResponseEntity<RedisDto> getUserStatus(@PathVariable("mobileNumber") String mobileNumber){
        return new ResponseEntity<>(userService.getUserStatus(mobileNumber),HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestParam("skipUrlGeneration") boolean skipUrlGeneration, @RequestBody GenerateUrlRequestDTO generateUrlRequestDTO){
        return new ResponseEntity<>(userService.authenticateUser(generateUrlRequestDTO, skipUrlGeneration), HttpStatus.MOVED_TEMPORARILY);
    }

    @PostMapping("/generateUrl")
    public ResponseEntity<?> generateUrl(@RequestBody MobileRequestDTO mobileRequestDTO){
        return new ResponseEntity<>(userService.generateUrl(mobileRequestDTO), HttpStatus.MOVED_TEMPORARILY);
    }

}