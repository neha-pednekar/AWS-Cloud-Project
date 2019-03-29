package com.csye6255.web.application.fall2018.utilities;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3BucketUtility {

    public  static AmazonS3  createCredentials() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
        return s3Client;
    }

    public static String productRetrieveFileFromS3( String fileName, String app_username , String bucketName) {
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
        S3Object retrievedPic = null;


        String storedPicName = new String();
        for( S3ObjectSummary sumObj : S3Objects.inBucket(s3client, bucketName) ) {
            storedPicName = sumObj.getKey();
            if( storedPicName.equals(app_username) ) {
                retrievedPic = s3client.getObject( bucketName, storedPicName );
                break;
            }
        }
        if( retrievedPic != null ) {
            return retrievedPic.getObjectContent().getHttpRequest().getURI().toString();
        }
        else {
            return null;
        }


    }



}
