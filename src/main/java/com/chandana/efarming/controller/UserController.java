package com.chandana.efarming.controller;

import com.chandana.efarming.model.Crop;
import com.chandana.efarming.model.User;
import com.chandana.efarming.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Create new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return userRepository.save(user);
    }

    // Upload profile picture (replace if already exists)
    @PostMapping("/{id}/upload-profile-pic")
    public ResponseEntity<String> uploadProfilePic(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        String contentType = file.getContentType();

        // ✅ Validate extension and MIME type
        if (!List.of(".jpg", ".jpeg", ".png").contains(extension) ||
                !List.of("image/jpeg", "image/png").contains(contentType)) {
            return ResponseEntity.badRequest().body("Only JPG, JPEG, and PNG files are allowed.");
        }

        // ✅ Sanitize email and build filename
        String sanitizedEmail = user.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
        String filename = sanitizedEmail + extension;

        // ✅ Define path: projectDir/attachments/users/
        String uploadDir = System.getProperty("user.dir") + File.separator + "attachments" + File.separator + "users" + File.separator;
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File dest = new File(uploadDir + filename);

        // ✅ Delete old image if it exists
        if (dest.exists()) {
            dest.delete();
        }

        try {
            file.transferTo(dest);
            user.setProfilePic("/attachments/users/" + filename); // Save relative path
            userRepository.save(user);
            return ResponseEntity.ok("Profile picture uploaded successfully: " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Delete profile picture file
        if (user.getProfilePic() != null) {
            String imagePath = System.getProperty("user.dir") + user.getProfilePic().replace("/", File.separator);
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        // ✅ Delete crop images for this user
        List<Crop> crops = user.getCrops(); // assuming you added the `getCrops()` getter
        for (Crop crop : crops) {
            if (crop.getImageUrl() != null) {
                String cropImagePath = System.getProperty("user.dir") + crop.getImageUrl().replace("/", File.separator);
                File cropFile = new File(cropImagePath);
                if (cropFile.exists()) {
                    cropFile.delete();
                }
            }
        }

        // ✅ Delete user (this also deletes all associated crops from DB due to cascade)
        userRepository.delete(user);
        return "User, profile picture, and all crop images deleted";
    }


    // Update user
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setProfilePic(updatedUser.getProfilePic()); // Optional: skip if unchanged
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }
}
