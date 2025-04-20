package ch.uzh.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageUploadUtil {

    private static final String UPLOAD_DIR = "target/uploads";

    public static String saveImage(MultipartFile file, String filename) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String filePath = UPLOAD_DIR + "/" + filename;
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return "/uploads/" + filename;
    }
}
