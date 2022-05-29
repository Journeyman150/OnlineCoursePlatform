package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class StorageService {

    public void store(MultipartFile multipartFile, String storagePath, String absolutePath) {
        File path = new File(storagePath);
        if (!path.exists()) {
            path.mkdir();
        }
        File file = new File(absolutePath);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public File load(String absolutePath) {
        return new File(absolutePath);
    }

    public void delete(String absolutePath) {
        File file = new File(absolutePath);
        file.delete();
    }
}
