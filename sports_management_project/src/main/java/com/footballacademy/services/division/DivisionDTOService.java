package com.footballacademy.services.division;

import com.footballacademy.DTO.DivisionDetailDTO;
import com.footballacademy.DTO.DivisionSummaryDTO;
import com.footballacademy.mappers.DivisionMapper;
import com.footballacademy.model.Division;
import com.footballacademy.repository.DivisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public
class DivisionDTOService {
    private final DivisionRepository divisionRepository;
    private final DivisionMapper mapper;
    public DivisionDTOService(DivisionRepository divisionRepository, DivisionMapper mapper) {
        this.divisionRepository = divisionRepository;
        this.mapper = mapper;
    }
    @Transactional(readOnly = true)
    public List<DivisionSummaryDTO> getSummaryList() {
        return divisionRepository.findAllBy() .stream() .map(mapper::toSummary) .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public DivisionDetailDTO getDetail(Long id) {
        Division d = divisionRepository.findWithPlayersById(id) .orElseThrow(() -> new RuntimeException("Division not found"));
        return mapper.toDetail(d);
    }
}
