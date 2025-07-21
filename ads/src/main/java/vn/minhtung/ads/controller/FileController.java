package vn.minhtung.ads.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.minhtung.ads.domain.file.ResUploadFileDTO;
import vn.minhtung.ads.service.FileService;
import vn.minhtung.ads.util.anotation.ApiMessage;
import vn.minhtung.ads.util.errors.StrogareExeption;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;

    @Value("${minhtung.upload-file.base-uri}")
    private String baseURI;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload File")
    public ResponseEntity<ResUploadFileDTO> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws URISyntaxException, IOException, StrogareExeption {

        if (file == null || file.isEmpty()) {
            throw new StrogareExeption("File is emty");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList(
                "jpg", "jpeg", "png", "gif", "pdf", "docx", "xlsx",
                "mp4", "avi", "mov", "wmv", "mkv", "webm");
        boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));

        if (isValid == false) {
            throw new StrogareExeption(
                    "File type not allowed, only support: " + allowedExtensions.toString());
        }

        this.fileService.createDirectory(baseURI + folder);

        String uploadFile = this.fileService.store(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadFile, Instant.now());
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/files")
    @ApiMessage("Download a file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws StrogareExeption, URISyntaxException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new StrogareExeption("Missing required params : (fileName or folder) in query params.");
        }

        long fileLength = this.fileService.getFileLength(fileName, folder);
        if (fileLength == 0) {
            throw new StrogareExeption("File with name = " + fileName + " not found.");
        }

        InputStreamResource resource = this.fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
