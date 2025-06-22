package com.chandana.efarming.repository;

import com.chandana.efarming.model.Crop;
import com.chandana.efarming.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByAvailableTrue();
    List<Crop> findByFarmer(User farmer);
}
