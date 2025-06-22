package com.chandana.efarming.controller;

import com.chandana.efarming.model.Crop;
import com.chandana.efarming.model.User;
import com.chandana.efarming.repository.CropRepository;
import com.chandana.efarming.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private UserRepository userRepository;

    // Create new crop
    @PostMapping
    public Crop createCrop(@RequestBody Crop crop) {
        if (crop.getName() == null || crop.getPrice() == 0 || crop.getQuantity() <= 0) {
            throw new RuntimeException("Missing required crop details");
        }

        if (crop.getFarmer() == null || crop.getFarmer().getId() == null ||
                !userRepository.existsById(crop.getFarmer().getId())) {
            throw new RuntimeException("Invalid farmer ID");
        }

        return cropRepository.save(crop);
    }

    // Upload crop image (replace if already exists)
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadCropImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String contentType = file.getContentType();

        if (!List.of(".jpg", ".jpeg", ".png").contains(extension) ||
                !List.of("image/jpeg", "image/png").contains(contentType)) {
            return ResponseEntity.badRequest().body("Only JPG, JPEG, and PNG files are allowed");
        }

        String safeCropName = (crop.getName() != null)
                ? crop.getName().replaceAll("[^a-zA-Z0-9]", "_")
                : "crop";

        String filename = id + "_" + safeCropName + extension;
        String uploadDir = System.getProperty("user.dir") + File.separator + "attachments" + File.separator + "crops" + File.separator;

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File dest = new File(uploadDir + filename);

        // ✅ If image already exists, delete it
        if (dest.exists()) {
            dest.delete();
        }

        try {
            file.transferTo(dest);
            crop.setImageUrl("/attachments/crops/" + filename);
            cropRepository.save(crop);
            return ResponseEntity.ok("Image uploaded successfully: " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }


    // Get all available crops
    @GetMapping
    public List<Crop> getAllCrops() {
        return cropRepository.findByAvailableTrue();
    }

    // Get crop by ID
    @GetMapping("/{id}")
    public Crop getCropById(@PathVariable Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));
    }

    @DeleteMapping("/{id}")
    public String deleteCrop(@PathVariable Long id) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        // Delete the image file if it exists
        if (crop.getImageUrl() != null) {
            String imagePath = System.getProperty("user.dir") + crop.getImageUrl().replace("/", File.separator);
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete(); // Delete image from attachments
            }
        }

        cropRepository.deleteById(id);
        return "Crop and associated image deleted";
    }

    // Update crop
    @PutMapping("/{id}")
    public Crop updateCrop(@PathVariable Long id, @RequestBody Crop updatedCrop) {
        Crop existingCrop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + id));

        existingCrop.setName(updatedCrop.getName());
        existingCrop.setDescription(updatedCrop.getDescription());
        existingCrop.setPrice(updatedCrop.getPrice());
        existingCrop.setQuantity(updatedCrop.getQuantity());
        existingCrop.setImageUrl(updatedCrop.getImageUrl());
        existingCrop.setAvailable(updatedCrop.isAvailable());

        if (updatedCrop.getFarmer() != null && updatedCrop.getFarmer().getId() != null) {
            User farmer = userRepository.findById(updatedCrop.getFarmer().getId())
                    .orElseThrow(() -> new RuntimeException("Farmer not found"));
            existingCrop.setFarmer(farmer);
        }

        return cropRepository.save(existingCrop);
    }
}
