package com.neo.rental.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 프론트에서 넘어온 파일을 S3에 저장하고, URL 주소를 반환하는 메서드
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        // 1. 파일 이름이 안 겹치게 고유한 이름(UUID) 생성
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFilename;

        // 2. 파일 크기 등을 S3에 알려주는 메타데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // 3. S3로 진짜 파일 업로드!
        amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);

        // 4. 업로드된 파일의 접속 URL (https://s3.ap-northeast-2...) 을 리턴
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}