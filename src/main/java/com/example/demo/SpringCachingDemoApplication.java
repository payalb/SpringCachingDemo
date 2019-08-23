package com.example.demo;

import java.io.File;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;

import com.example.demo.service.AWSS3Helper;
import com.example.demo.service.AWSS3HelperImpl;

@SpringBootApplication
@EnableCaching(proxyTargetClass=true)
public class SpringCachingDemoApplication {

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx=SpringApplication.run(SpringCachingDemoApplication.class, args);
		AWSS3HelperImpl bean=(AWSS3HelperImpl) ctx.getBean("AWSS3HelperImpl");
	
		File file1=bean.getObjectFromUrl("01_men_one.jpg");
		File file2= bean.getObjectFromUrl("01_men_one.jpg");
	}

}
