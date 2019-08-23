package com.example.demo.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.example.demo.AWSConfig;

@Service("AWSS3HelperImpl")
public class AWSS3HelperImpl implements AWSS3Helper {

	@Autowired
	AWSConfig awsConfig;
	private Credentials sessionCredentials;

	@PostConstruct
	private void init() {

	}

	private BasicSessionCredentials getBasicSessionCredentials() {
		if (sessionCredentials == null || sessionCredentials.getExpiration().before(new Date()))
			sessionCredentials = getSessionCredentials();

		return new BasicSessionCredentials(sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(),
				sessionCredentials.getSessionToken());
	}

	private Credentials getSessionCredentials() {
		AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(
				new BasicAWSCredentials(awsConfig.getAws().getAccessKeyId(), awsConfig.getAws().getAccessKeySecret()));
		GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest().withDurationSeconds(43200);
		this.sessionCredentials = stsClient.getSessionToken(getSessionTokenRequest).getCredentials();
		return this.sessionCredentials;
	}

	public AmazonS3 getAmazonS3Client() {
		BasicSessionCredentials basicSessionCredentials = getBasicSessionCredentials();
		return AmazonS3ClientBuilder.standard().withRegion(awsConfig.getS3().getRegion())
				.withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials)).build();
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	@CachePut(key="#root.args[1]",value="imageStore")
	@Override
	public String putObject(MultipartFile file, String fileName) throws AmazonServiceException, SdkClientException, IOException {
		getAmazonS3Client().putObject(awsConfig.getS3().getDefaultBucket(), file.getName(),
				convertMultiPartToFile(file));
		return awsConfig.getS3().getEndPoint() + "/" + awsConfig.getS3().getDefaultBucket() + "/" + file.getName();
	}

	@Override
	@Cacheable(key="#root.args[0]", value="imageStore")
	public File getObjectFromUrl(String filename) throws IOException {
		System.out.println("Getting from s3");
		File file = new File(filename);
		S3Object o = getAmazonS3Client().getObject(awsConfig.getS3().getDefaultBucket(), filename);
		try (FileOutputStream fos = new FileOutputStream(new File(filename));
			 BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(file));) {
			InputStream s3is = o.getObjectContent();
			int i = s3is.read();
			while (i != -1) {
				bs.write(i);
				i = s3is.read();
			}
		}
		return file;
	}

	@CacheEvict(key="#root.args[0]", value="imageStore")
	@Override
	public String deleteFileByKey(String fileName) {
		getAmazonS3Client().deleteObject(new DeleteObjectRequest(awsConfig.getS3().getDefaultBucket(), fileName));
		return "Successfully deleted";
	}

	
}
