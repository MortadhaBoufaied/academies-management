package com.footballacademy.controllers_rest.division;

import com.footballacademy.DTO.DivisionDetailDTO;
import com.footballacademy.DTO.DivisionSummaryDTO;
import com.footballacademy.services.division.DivisionDTOService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dto/divisions")
public
class DivisionDTOController {
    private final DivisionDTOService service;
    public DivisionDTOController(DivisionDTOService service) {
        this.service = service;
    }
    @GetMapping
    public List<DivisionSummaryDTO> getAll() {
        return service.getSummaryList();
    }
    @GetMapping("/{id}")
    public DivisionDetailDTO getOne(
    @PathVariable Long id) {
        return service.getDetail(id);
    }
}
