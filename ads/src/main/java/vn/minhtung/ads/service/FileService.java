package vn.minhtung.ads.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Value("${minhtung.upload-file.base-uri}")
    private String baseURI;

    public void createDirectory(String folder) {
        try {
            URI uri = URI.create(folder.trim());
            Path path = Paths.get(uri);
            File tmpDir = path.toFile();

            if (!tmpDir.isDirectory()) {
                Files.createDirectories(path);
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.getAbsolutePath());
            } else {
                System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
            }
        } catch (Exception e) {
            System.err.println(">>> ERROR WHEN CREATING DIRECTORY:");
            e.printStackTrace();
        }
    }

    public String store(MultipartFile file, String folder) throws IOException {

        folder = folder.trim().replace("\\", "/").replaceAll("[\n\r]+", "");
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        URI base = URI.create(baseURI.trim());
        Path basePath = Paths.get(base);
        Path folderPath = basePath.resolve(folder);
        Path filePath = folderPath.resolve(finalName);
        Files.createDirectories(folderPath);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File tmpDir = new File(path.toString());

        // file không tồn tại, hoặc file là 1 director => return 0
        if (!tmpDir.exists() || tmpDir.isDirectory())
            return 0;
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
}