package com.footballacademy.mappers;

import com.footballacademy.DTO.DivisionDetailDTO;
import com.footballacademy.DTO.DivisionSummaryDTO;
import com.footballacademy.DTO.PlayerDTO;
import com.footballacademy.model.Division;
import com.footballacademy.model.Player;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public
class DivisionMapper {
    public DivisionSummaryDTO toSummary(Division d) {
        var players = d.getPlayers() == null ? Collections.<Player>emptyList() : d.getPlayers();
        int playerCount = players.size();
        double avgAge = players.stream() .map(Player::getAge) .filter(Objects::nonNull) .mapToInt(Integer::intValue) .average() .orElse(0);
        return new DivisionSummaryDTO(d.getId(), d.getNom(), d.getCategorie(), playerCount, avgAge);
    }
    public DivisionDetailDTO toDetail(Division d) {
        DivisionDetailDTO dto = new DivisionDetailDTO();
        dto.setId(d.getId());
        dto.setNom(d.getNom());
        dto.setCategorie(d.getCategorie());
        var players = d.getPlayers() == null ? Collections.<Player>emptyList() : d.getPlayers();
        dto.setPlayerCount(players.size());
        dto.setAverageAge(players.stream() .map(Player::getAge) .filter(Objects::nonNull) .mapToInt(Integer::intValue) .average() .orElse(0));
        dto.setPlayers(players.stream() .map(p -> {
            PlayerDTO pd = new PlayerDTO();
            pd.setId(p.getId());
            pd.setNom(p.getUser() != null ? p.getUser() .getNom() : null);
            pd.setPosition(p.getPosition());
            pd.setAge(p.getAge());
            pd.setPaid(p.isPaid());
            pd.setImageUrl(p.getImageUrl());
            return pd;
        }) .collect(Collectors.toList()));
        return dto;
    }
}
