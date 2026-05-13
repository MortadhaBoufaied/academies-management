package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlayerAttributeSnapshotRepository extends JpaRepository<PlayerAttributeSnapshot, Long> { List<PlayerAttributeSnapshot> findByPlayer_Id(Long playerId); }
