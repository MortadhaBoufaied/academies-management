package com.footballacademy.services.file;

import com.footballacademy.config.data.FileStorageConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
public
class FileStorageService {
    private final Path fileStorageLocation;
    private final long maxFileSize = 10 * 1024 * 1024;
    // 10MB
    private final Set<String> allowedExtensions = Set.of(".pdf", ".docx", ".png", ".jpg", ".jpeg");
    public FileStorageService(FileStorageConfig config) {
        this.fileStorageLocation = Paths.get(config.fileStorageLocation()) .toAbsolutePath() .normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    /** Backward compatible method (stores under misc). */
    public String storeFile(MultipartFile file, String description, Long uploadedBy) {
        return storeFile(file, "misc", description, uploadedBy);
    }
    /**      * Stores under {base}/{category}/{uploadedBy}/ and returns a public URL path: /uploads/{category}/{uploadedBy}/{file}      * Images are compressed and converted to JPG.      */
    public String storeFile(MultipartFile file, String category, String description, Long uploadedBy) {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        try {
            if (original.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + original);
            }
            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("File too large. Max allowed size is " + maxFileSize + " bytes");
            } String fileExtension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) fileExtension = original.substring(dot) .toLowerCase();
            if (!allowedExtensions.contains(fileExtension)) {
                throw new RuntimeException("File type not allowed: " + fileExtension);
            } String safeCategory = sanitizeSegment(category);
            String userSegment = uploadedBy == null ? "0" : String.valueOf(uploadedBy);
            Path categoryDir = this.fileStorageLocation.resolve(safeCategory) .resolve(userSegment) .normalize();
            Files.createDirectories(categoryDir);
            boolean isImage = fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".jpeg");
            String newFileName;
            Path targetLocation;
            if (isImage) {
                newFileName = UUID.randomUUID() + ".jpg";
                targetLocation = categoryDir.resolve(newFileName);
                compressAndSaveJpeg(file, targetLocation, 0.78f, 1280);
            } else {
                newFileName = UUID.randomUUID() + fileExtension;
                targetLocation = categoryDir.resolve(newFileName);
                try(InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }
            } return "/uploads/" + safeCategory + "/" + userSegment + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + original + ". Please try again!", ex);
        }
    }
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName) .normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName) .normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
    private static String sanitizeSegment(String raw) {
        if (raw == null || raw.isBlank()) return "misc";
        String s = raw.trim() .toLowerCase();
        s = s.replaceAll("[^a-z0-9_-]", "_");
        if (s.isBlank()) s = "misc";
        return s;
    }
    private static void compressAndSaveJpeg(MultipartFile file, Path target, float quality, int maxDim) throws IOException {
        BufferedImage input;
        try(InputStream in = file.getInputStream()) {
            input = ImageIO.read(in);
        }
        if (input == null) throw new RuntimeException("Unsupported image format");
        BufferedImage rgb = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = rgb.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g2.drawImage(input, 0, 0, null);
        g2.dispose();
        int w = rgb.getWidth();
        int h = rgb.getHeight();
        int max = Math.max(w, h);
        double scale = max > maxDim ?(double) maxDim /(double) max : 1.0;
        BufferedImage outImg = rgb;
        if (scale < 1.0) {
            int nw =(int) Math.round(w * scale);
            int nh =(int) Math.round(h * scale);
            BufferedImage resized = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(rgb, 0, 0, nw, nh, null);
            g.dispose();
            outImg = resized;
        } Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) throw new RuntimeException("No JPEG writer available");
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(Math.max(0.1f, Math.min(quality, 1.0f)));
        } Files.createDirectories(target.getParent());
        try(ImageOutputStream ios = ImageIO.createImageOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(outImg, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
