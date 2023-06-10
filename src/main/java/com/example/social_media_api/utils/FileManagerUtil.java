package com.example.social_media_api.utils;

import com.example.social_media_api.exception.FileManagerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileManagerUtil {
    @Value("${upload.path}")
    private String uploadPath;

    public String saveFileAndGetLink(MultipartFile image) throws FileManagerException, IllegalArgumentException {
        if (image == null || image.getOriginalFilename().isEmpty() || uploadPath == null) {
            return null;
        }

        try {
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);

            String uuidFile = UUID.randomUUID().toString();
            String originFileName = image.getOriginalFilename();
            String extension = originFileName.substring(originFileName.lastIndexOf(".")).toLowerCase();

            if (!extension.matches("\\.(jpg|jpeg|png)")) {
                throw new IllegalArgumentException("Invalid image format. Only JPG, JPEG, and PNG formats are allowed");
            }

            String resultFileName = uuidFile + extension;
            Path filePath = Paths.get(uploadPath, resultFileName);
            image.transferTo(Files.createFile(filePath));

            return resultFileName;
        } catch (IOException e) {
            throw new FileManagerException("An error occurred while saving file");
        }
    }

    public void deleteFile(String filename) throws FileManagerException {
        try {
            if (filename != null) {
                Path filePath = Paths.get(uploadPath, filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }
        } catch (IOException e) {
            throw new FileManagerException("An error occurred while deleting file");
        }
    }
}
