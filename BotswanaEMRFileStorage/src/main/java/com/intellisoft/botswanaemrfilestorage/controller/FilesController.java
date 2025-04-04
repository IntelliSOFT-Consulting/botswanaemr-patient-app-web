package com.intellisoft.botswanaemrfilestorage.controller;

import com.intellisoft.botswanaemrfilestorage.model.Files;
import com.intellisoft.botswanaemrfilestorage.payload.FileUploadResponse;
import com.intellisoft.botswanaemrfilestorage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Tag(description = "Use these resource to save file in the system.", name = "File system module")
@RequestMapping(value = "/filestorage/api/v1")
@RestController
public class FilesController {

    @Autowired
    private FileStorageService fileStorageService;


    @Operation(summary = "Upload a File", description = "Uploads a file to the server's file storage system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PostMapping("/upload-file")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        Files files = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/filestorage/api/v1/filestorage/")
                .path(files.getId())
                .toUriString();

        return new FileUploadResponse(
                files.getId(),
                files.getName(),
                fileDownloadUri,
                file.getContentType(),
                file.getSize());
    }

    @Operation(summary = "Download a File", description = "Download a file to the server's file system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping("/filestorage/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        Files dbFile = fileStorageService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "/attachment; filename=\"" + dbFile.getName() + "\"")
                .body(new ByteArrayResource(dbFile.getFilecontent()));
    }



    @Autowired
    private HttpServletRequest request;

    @GetMapping("/server-url")
    public String getServerUrl() {
        String serverUrl = request.getRequestURL().toString();
        return "Server URL: " + serverUrl;
    }
}


