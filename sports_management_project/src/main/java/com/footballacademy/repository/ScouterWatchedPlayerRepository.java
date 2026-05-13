package com.footballacademy.repository;

import com.footballacademy.model.ScouterWatchedPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScouterWatchedPlayerRepository extends JpaRepository<ScouterWatchedPlayer, Long> {
    List<ScouterWatchedPlayer> findByScouter_Id(Long scouterId);
    List<ScouterWatchedPlayer> findByScouter_IdAndWatchStatus(Long scouterId, String watchStatus);
    List<ScouterWatchedPlayer> findByScouter_IdAndSport_Id(Long scouterId, Long sportId);
    List<ScouterWatchedPlayer> findByScouter_IdAndAcademy_Id(Long scouterId, Long academyId);
    List<ScouterWatchedPlayer> findByScouter_IdAndDivision_Id(Long scouterId, Long divisionId);
    Optional<ScouterWatchedPlayer> findByScouter_IdAndPlayer_Id(Long scouterId, Long playerId);

    default List<ScouterWatchedPlayer> findByScouterId(Long scouterId) {
        return findByScouter_Id(scouterId);
    }

    default List<ScouterWatchedPlayer> findByScouterIdAndWatchStatus(Long scouterId, String watchStatus) {
        return findByScouter_IdAndWatchStatus(scouterId, watchStatus);
    }

    default List<ScouterWatchedPlayer> findByScouterIdAndSportId(Long scouterId, Long sportId) {
        return findByScouter_IdAndSport_Id(scouterId, sportId);
    }

    default List<ScouterWatchedPlayer> findByScouterIdAndAcademyId(Long scouterId, Long academyId) {
        return findByScouter_IdAndAcademy_Id(scouterId, academyId);
    }

    default List<ScouterWatchedPlayer> findByScouterIdAndDivisionId(Long scouterId, Long divisionId) {
        return findByScouter_IdAndDivision_Id(scouterId, divisionId);
    }

    default Optional<ScouterWatchedPlayer> findByScouterIdAndPlayerId(Long scouterId, Long playerId) {
        return findByScouter_IdAndPlayer_Id(scouterId, playerId);
    }
}
