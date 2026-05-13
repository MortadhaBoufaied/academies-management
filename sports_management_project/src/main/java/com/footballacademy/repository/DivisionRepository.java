package com.footballacademy.repository;

import com.footballacademy.model.Division;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface DivisionRepository extends JpaRepository<Division, Long> {
    List<Division> findByCategorie(String categorie);
    List<Division> findByAcademy_Id(Long academyId);
    List<Division> findByAcademy_IdAndCategorie(Long academyId, String categorie);
    List<Division> findBySport_IdOrderByDisplayOrderAscNomAsc(Long sportId);
    List<Division> findBySport_IdAndActiveTrueOrderByDisplayOrderAscNomAsc(Long sportId);
    Optional<Division> findByNomIgnoreCase(String nom);
    Optional<Division> findByNomIgnoreCaseAndCategorieIgnoreCase(String nom, String categorie);
    Optional<Division> findBySport_IdAndNomIgnoreCaseAndCategorieIgnoreCase(Long sportId, String nom, String categorie);
    @EntityGraph(attributePaths = {
        "players"
    }) List<Division> findAllBy();
    // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ FIX: move ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“WithPlayersÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â BEFORE ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ByIdÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â
    @EntityGraph(attributePaths = {
        "players"
    }) Optional<Division> findWithPlayersById(Long id);
    @org.springframework.data.jpa.repository.Query("select d from Division d where lower(d.nom) like lower(concat('%', :q, '%'))") java.util.List<Division> searchByNom(
    @org.springframework.data.repository.query.Param("q") String q);
}
