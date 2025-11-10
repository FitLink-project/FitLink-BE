package com.fitlink.awsS3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /**
     * S3 ?뚯씪 ?낅줈??諛?URL 諛섑솚
     */
    public String uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "?낅줈?쒗븷 ?뚯씪???놁뒿?덈떎.");
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 ?뚯씪 ?낅줈???ㅽ뙣");
        }

        return getFileUrl(fileName);
    }

    /**
     * ?뚯씪紐??쒖닔??+ ?뺤옣???좎?
     */
    private String createFileName(String originalName) {
        return UUID.randomUUID().toString() + getFileExtension(originalName);
    }

    /**
     * ?뚯씪 ?뺤옣??異붿텧
     */
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "?섎せ???뚯씪 ?뺤떇: " + fileName);
        }
    }

    /**
     * S3???낅줈?쒕맂 ?뚯씪??URL 諛섑솚
     */
    public String getFileUrl(String fileName) {
        URL url = amazonS3.getUrl(bucket, fileName);
        return url.toString();
    }

    /**
     * ?뚯씪紐?湲곗??쇰줈 ?뚯씪 ??젣
     */
    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 ?뚯씪 ??젣 ?ㅽ뙣");
        }
    }

    /**
     * URL 湲곗??쇰줈 ?뚯씪 ??젣
     */
    public void deleteFileByUrl(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 ?뚯씪 ??젣 ?ㅽ뙣");
        }
    }

    /**
     * URL?먯꽌 ?뚯씪紐?異붿텧
     */
    private String extractFileNameFromUrl(String url) {
        try {
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            return decodedUrl.substring(decodedUrl.lastIndexOf("/") + 1);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL?먯꽌 ?뚯씪紐낆쓣 異붿텧?????놁뒿?덈떎.");
        }
    }
}
