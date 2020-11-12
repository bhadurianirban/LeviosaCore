/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.bl.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hedwig.cloud.response.HedwigResponseCode;
import org.hedwig.cms.dto.AwsS3DTO;

/**
 *
 * @author bhaduri
 */
public class AwsS3Service {

    private final AWSCredentials credentials;

    public AwsS3Service(String accessKey, String secretKey) {
        credentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    public  AwsS3DTO uploadToS3(AwsS3DTO awsS3DTO) {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        File fileToUpload = new File(awsS3DTO.getUploadFromLocalPath() + File.separator + awsS3DTO.getUploadLocalFileName());
        try {
            s3.putObject(new PutObjectRequest(awsS3DTO.getAWSBucketName(), awsS3DTO.getAWSKeyName(), fileToUpload));
            
            String fileUrl = s3.getUrl(awsS3DTO.getAWSBucketName(), awsS3DTO.getAWSKeyName()).toString();
            awsS3DTO.setS3bucketUrl(fileUrl);
            awsS3DTO.setResponseCode(HedwigResponseCode.SUCCESS);
        } catch (AmazonServiceException ase) {
            Logger.getLogger(AwsS3Service.class.getName()).log(Level.SEVERE, null, ase);
            awsS3DTO.setResponseCode(HedwigResponseCode.AmazonServiceException);
            

        } catch (AmazonClientException ace) {
            Logger.getLogger(AwsS3Service.class.getName()).log(Level.SEVERE, null, ace);
            awsS3DTO.setResponseCode(HedwigResponseCode.AmazonClientException);
        }
        return awsS3DTO;
    }
    public AwsS3DTO deleteFromS3(AwsS3DTO awsS3DTO) {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                        .withRegion(Regions.US_WEST_2)
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .build();
        try {
            s3.deleteObject(awsS3DTO.getAWSBucketName(), awsS3DTO.getAWSKeyName());
            
            awsS3DTO.setResponseCode(HedwigResponseCode.SUCCESS);
        } catch (SdkClientException e) {
            awsS3DTO.setResponseCode(HedwigResponseCode.AmazonClientException);
        }

        return awsS3DTO;
    }

}
