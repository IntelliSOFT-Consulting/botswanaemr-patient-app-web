package com.intellisoft.botswanaemrfilestorage.service;


import com.intellisoft.botswanaemrfilestorage.exception.FileNotFoundException;
import com.intellisoft.botswanaemrfilestorage.exception.FileStorageException;
import com.intellisoft.botswanaemrfilestorage.model.Files;
import com.intellisoft.botswanaemrfilestorage.repo.FileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    @Autowired
    private FileRepo fileRepo;

    public Files storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        //Check if the file size is not greater than 1000MB
        if (file.getSize() > 1000000000) {
            throw new FileStorageException("File size is too large. Maximum file size is 1GB");
        }

        try {
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequenth " + fileName);
            }

            Files dbFile = new Files(
                    fileName,
                    file.getContentType(),
                    file.getBytes()
            );

            return fileRepo.save(dbFile);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Files getFile(String fileId) {
        return fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File you requested not found with id " + fileId));
    }
}