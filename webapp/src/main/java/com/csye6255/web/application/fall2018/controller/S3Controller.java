package com.csye6255.web.application.fall2018.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye6255.web.application.fall2018.dao.AttachmentDAO;
import com.csye6255.web.application.fall2018.dao.TransactionDAO;
import com.csye6255.web.application.fall2018.dao.UserDAO;
import com.csye6255.web.application.fall2018.pojo.Attachment;
import com.csye6255.web.application.fall2018.pojo.Transaction;
import com.csye6255.web.application.fall2018.pojo.User;
import com.csye6255.web.application.fall2018.utilities.AuthorizationUtility;
import com.csye6255.web.application.fall2018.utilities.S3BucketUtility;
import com.google.gson.JsonObject;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author rkirouchenaradjou
 */
@Controller
@Profile("aws")
public class S3Controller {

    private final static Logger logger = LoggerFactory.getLogger(S3Controller.class);

    @Autowired
    UserDAO userDao;

    @Autowired
    TransactionDAO transactionDAO;

    @Autowired
    AttachmentDAO attachmentDAO;

    @Autowired
    private Environment env;

    @Autowired
    private StatsDClient statsDClient;

    @RequestMapping(value = "/transaction/{transactionid}/attachments", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity attachFilesToTransaction(@PathVariable("transactionid") String transactionid,
                                                   HttpServletRequest request, @RequestParam("uploadReceipt") MultipartFile uploadReceiptFile) throws FileNotFoundException, IOException {
        statsDClient.incrementCounter("endpoint.test.http.post");
        JsonObject jsonObject = new JsonObject();
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String[] values = AuthorizationUtility.getHeaderValues(authorization);
            String userName = values[0];
            String password = values[1];
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            List<User> userList = userDao.findByUserName(userName);

            if (userList.size() != 0) {
                User user = userList.get(0);
                if (encoder.matches(password, user.getPassword())) {
                    List<Transaction> transactionList = transactionDAO.findByTransactionid(transactionid);
                    if (transactionList.size() != 0) {
                        Transaction trans = transactionList.get(0);
                        if (trans.getUser().getId() == user.getId()) {
                            if (uploadReceiptFile.isEmpty()) {
                                logger.error("attachFilesToTransaction Method : No file to upload");
                                jsonObject.addProperty("message", "No file to upload");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject.toString());
                            }
                            if (!uploadReceiptFile.isEmpty()) {
                                String filename = uploadReceiptFile.getOriginalFilename();
                                String suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                                String newFileName = System.currentTimeMillis() + "." + suffix;
                                if (!suffix.equals("png") && !suffix.equals("jpg") && !suffix.equals("jpeg")) {
                                    String errMsg = "Please upload image file( supported type: *.png/*.jpeg/*.jpg )";
                                    logger.info(errMsg);
                                    jsonObject.addProperty("message", errMsg);
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject.toString());

                                } else {
                                    AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
                                    try {
                                        String path = System.getProperty("user.dir") + "/images";
                                        String filePath = path + "/";
                                        InputStream is = uploadReceiptFile.getInputStream();
                                        s3.putObject(new PutObjectRequest(env.getProperty("bucket.name"), newFileName, is, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead));
                                        String url=S3BucketUtility.productRetrieveFileFromS3("",newFileName,env.getProperty("bucket.name"));
                                        // Storing meta data in the DB: MSQL
                                        Attachment attachmentNew = new Attachment();
                                        attachmentNew.setTransaction(trans);
                                        attachmentNew.setUrl(url);
                                        attachmentDAO.save(attachmentNew);
                                        jsonObject.addProperty("id", attachmentNew.getAttachmentid());
                                        jsonObject.addProperty("url", attachmentNew.getUrl());
                                        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                                    } catch (AmazonServiceException e) {
                                        System.err.println(e.getErrorMessage());
                                        jsonObject.addProperty("message", e.getErrorMessage());
                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
                                    }
                                }
                            } else {
                                jsonObject.addProperty("message", "Upload Recepiet file is empty!!!");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject.toString());
                            }
                        } else {
                            jsonObject.addProperty("message", "No transaction found for the user");
                            return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                        }
                    }else{
                            jsonObject.addProperty("message", "No transaction found for the user");
                            return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                        }
                    } else {
                        jsonObject.addProperty("message", "Incorrect Password");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
                    }
                } else {
                    jsonObject.addProperty("message", "User not found! - Try Logging in again");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
                }
            } else {
                jsonObject.addProperty("message", "You are not logged in - Provide Username and Password!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
            }
        }

    @RequestMapping(value = "/transaction/{transactionid}/attachments/{attachmentid}", method = RequestMethod.PUT, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity replaceAttachmentInTransaction(@PathVariable("transactionid") String transactionid,
                                            @PathVariable("attachmentid") String attachmentid, HttpServletRequest request,
                                            @RequestParam("uploadReceipt") MultipartFile uploadReceiptFile) throws FileNotFoundException, IOException {
        statsDClient.incrementCounter("endpoint.test.http.put");
        JsonObject jsonObject = new JsonObject();
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String[] values = AuthorizationUtility.getHeaderValues(authorization);
            String userName = values[0];
            String password = values[1];
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            List<User> userList = userDao.findByUserName(userName);

            if (userList.size() != 0) {
                User user = userList.get(0);
                if (encoder.matches(password, user.getPassword())) {
                    List<Transaction> transactionList = transactionDAO.findByTransactionid(transactionid);
                    if (transactionList.size() != 0) {
                        Transaction trans = transactionList.get(0);
                        if (trans.getUser().getId() == user.getId()) {
                            if (uploadReceiptFile.isEmpty()) {
                                logger.error("replaceAttachmentInTransaction Method : No file to upload");
                            }
                            if (!uploadReceiptFile.isEmpty()) {
                                String filename = uploadReceiptFile.getOriginalFilename();
                                String suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                                String newFileName = System.currentTimeMillis() + "." + suffix;
                                if (!suffix.equals("png") && !suffix.equals("jpg") && !suffix.equals("jpeg")) {
                                    String errMsg = "Please upload image file( supported type: *.png/*.jpeg/*.jpg )";
                                    logger.info(errMsg);
                                    jsonObject.addProperty("message", errMsg);
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject.toString());

                                } else {
                                    AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
                                    List<Attachment> attachmentList = attachmentDAO.findByTransaction(trans);

                                    try {

                                        for (Attachment attachment1 : attachmentList) {
                                            if (attachment1.getAttachmentid().equals(attachmentid)) {
                                                InputStream is = uploadReceiptFile.getInputStream();
                                                String[] value =attachment1.getUrl().split("/"+env.getProperty("bucket.name"));
                                                String[] keyValue = value[1].split("/");
                                                s3.putObject(new PutObjectRequest(env.getProperty("bucket.name"), keyValue[1], is, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead));
                                                // Storing meta data in the DB: MSQL
                                                String newUrl=S3BucketUtility.productRetrieveFileFromS3("",keyValue[1],env.getProperty("bucket.name"));
                                                attachment1.setUrl(newUrl);
                                                attachmentDAO.save(attachment1);
                                            }
                                        }
                                        jsonObject.addProperty("message", "Attachment Successfully updated!");
                                        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                                           } catch (AmazonServiceException e) {
                                        System.err.println(e.getErrorMessage());
                                        jsonObject.addProperty("message", e.getErrorMessage());
                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
                                    }
                                }
                            } else
                                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                        } else {
                            jsonObject.addProperty("message", "No transaction found for the user");
                            return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                        }
                    } else {
                        jsonObject.addProperty("message", "Incorrect Password");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
                    }
                } else {
                    jsonObject.addProperty("message", "User not found! - Try Logging in again");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
                }
            } else {
                jsonObject.addProperty("message", "You are not logged in - Provide Username and Password!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
            }
        }
        else {
            jsonObject.addProperty("message", "You are not logged in - Provide Username and Password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
        }
    }

    @RequestMapping(value = "/transaction/{transactionid}/attachments/{attachmentid}", method = RequestMethod.DELETE, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity deleteAttachment(@PathVariable("transactionid") String transactionid,
                                           @PathVariable("attachmentid") String attachmentid, HttpServletRequest request) throws FileNotFoundException, IOException {
        statsDClient.incrementCounter("endpoint.test.http.delete");
        JsonObject jsonObject = new JsonObject();
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String[] values = AuthorizationUtility.getHeaderValues(authorization);
            String userName = values[0];
            String password = values[1];
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            List<User> userList = userDao.findByUserName(userName);

            if (userList.size() != 0) {
                User user = userList.get(0);
                if (encoder.matches(password, user.getPassword())) {
                    List<Transaction> transactionList = transactionDAO.findByTransactionid(transactionid);
                    if (transactionList.size() != 0) {
                        Transaction trans = transactionList.get(0);
                        if (trans.getUser().getId() == user.getId()) {
                            List<Attachment> attachmentList = attachmentDAO.findAttachmentByAttachmentid(attachmentid);

                            if (attachmentList.size() != 0) {
                                for(Attachment attachment : attachmentList) {
                                    try {
                                        if (attachment.getUrl()!=null) {
                                            String[] value =attachment.getUrl().split("/"+env.getProperty("bucket.name"));
                                            String[] keyValue = value[1].split("/");
                                            AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
                                            String toDelete = "";
                                            for(S3ObjectSummary summary: S3Objects.inBucket(s3client, env.getProperty("bucket.name"))){
                                                String imageName = summary.getKey();
                                                if(imageName.equals(keyValue[1])){
                                                    toDelete = imageName;
                                                    attachmentDAO.delete(attachment);
                                                    break;
                                                }
                                            }
                                            if(!toDelete.equals("")){
                                                s3client.deleteObject(env.getProperty("bucket.name"), toDelete);
                                            }
                                            jsonObject.addProperty("message", "File deleted successfully");
                                            return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                                        } else {
                                            logger.error("deleteAttachment Method : No file to delete");
                                        }
                                    } catch (Exception ex) {
                                        jsonObject.addProperty("message", "Error while storing in local storage " + ex.getMessage());
                                        logger.error("deleteAttachment Method : exception" + ex.getMessage());

                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
                                    }
                                }


                            } else {
                                jsonObject.addProperty("message", "No attachments found to delete");
                                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                            }
                        } else {
                            jsonObject.addProperty("message", "User not found! - Try Logging in again");
                            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                        }
                    } else {
                        jsonObject.addProperty("message", "No transaction found for the user");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject.toString());
                    }
                } else {
                    jsonObject.addProperty("message", "Incorrect Password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
                }
            } else {
                jsonObject.addProperty("message", "User not found! - Try Logging in again");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
            }
        } else {
            jsonObject.addProperty("message", "You are not logged in - Provide Username and Password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject.toString());
        }
        jsonObject.addProperty("message", "Attachment Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject.toString());
    }

        @RequestMapping( value = "/user/resetPassword", method = { RequestMethod.POST }, produces = {"application/json"} )
        private ResponseEntity resetUserPassword(@RequestHeader HttpHeaders headers, HttpServletRequest request ) {
        statsDClient.incrementCounter("endpoint.test.http.post");
        final String authorization = request.getHeader("Authorization");
            JsonObject jsonObject = new JsonObject();
            if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
                String[] values = AuthorizationUtility.getHeaderValues(authorization);
                String userName = values[0];
                String password = values[1];
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                List<User> userList = userDao.findByUserName(userName);
                if ((userList.size() != 0)||(!(password != null && password.length() == 0))) {
                    User user = userList.get(0);
                    if (encoder.matches(password, user.getPassword())) {
                        AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
                        String resetEmail = user.getUserName();
                        logger.info( "Reset Email: " + resetEmail );
                        String topicArn = env.getProperty("sns.arn");
                        PublishRequest publishRequest = new PublishRequest(topicArn, resetEmail);
                        PublishResult publishResult = snsClient.publish(publishRequest);
                        logger.info( "SNS Publish Result: " + publishResult );
                        jsonObject.addProperty("message", "Password Reset Link was sent to your emailID");
                        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                        }
                        else {
                        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                    }
                    } else {
                    jsonObject.addProperty("message", "Account does not exists for this user");
                    return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                }
                } else
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }


}
