package com.anios.ipification;

import com.anios.ipification.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

@EnableFeignClients
@SpringBootApplication
public class IpificationApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(IpificationApplication.class, args);

		RedisService redisService = context.getBean(RedisService.class);

		redisService.saveDataToRedis("5b4dcd2613944553b42124ab6d481619", "IP");
	}

}
