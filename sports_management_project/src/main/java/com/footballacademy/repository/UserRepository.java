package com.footballacademy.repository;

import com.footballacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findByMainRole(User.UserRole mainRole);
    long countByMainRole(User.UserRole mainRole);
    long countByActiveTrue();
    List<User> findByAcademy_Id(Long academyId);
    List<User> findByAcademy_IdAndMainRole(Long academyId, User.UserRole mainRole);
    List<User> findByAcademy_IdAndNomContainingIgnoreCaseOrAcademy_IdAndEmailContainingIgnoreCase(Long academyIdForName, String nom, Long academyIdForEmail, String email);
    @Query("select u from User u where lower(u.nom) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%'))") List<User> searchByNomOrEmail(
    @Param("q") String q);
    java.util.List<com.footballacademy.model.User> findTop30ByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email);
}
