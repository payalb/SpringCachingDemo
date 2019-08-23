package com.example.demo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;

public interface AWSS3Helper {
	
	File getObjectFromUrl(String filename) throws  IOException;
	String putObject(MultipartFile file, String filename) throws AmazonServiceException, SdkClientException, IOException;
	String deleteFileByKey(String fileName);
	
}
