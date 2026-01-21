package com.neo.rental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 업로드 처리
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 1. 파일명 중복 방지 (UUID 사용)
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + originalFilename;

        // 2. 저장할 경로 생성
        File savePath = new File(uploadDir + savedFileName);

        // 3. 파일 저장 실행
        file.transferTo(savePath);

        // 4. DB에 저장할 웹 접근 경로 반환 (나중에 프론트에서 이 경로로 이미지 접근)
        return "/images/" + savedFileName;
    }
}