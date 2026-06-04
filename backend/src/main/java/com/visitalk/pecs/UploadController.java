package com.visitalk.pecs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/jpg", "image/png");
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads");

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds 5MB limit"));
        }
        String type = file.getContentType();
        if (type == null || !ALLOWED.contains(type.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPG or PNG files are allowed"));
        }

        String ext = type.endsWith("png") ? ".png" : ".jpg";
        String name = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Files.createDirectories(UPLOAD_DIR);
            Path target = UPLOAD_DIR.resolve(name);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file"));
        }

        return ResponseEntity.ok(Map.of("url", "/uploads/" + name));
    }
}
