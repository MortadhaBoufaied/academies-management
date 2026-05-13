package com.footballacademy.controllers_rest.sport;

import com.footballacademy.model.SportCategory;
import com.footballacademy.services.sport.SportCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/sport-categories")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class SportCategoryController {
    private final SportCategoryService sportCategoryService;
    public SportCategoryController(SportCategoryService sportCategoryService) {
        this.sportCategoryService = sportCategoryService;
    }
    public
    record SportCategoryRequest(String code, String name, String description, Long sportId, Boolean isActive, Integer displayOrder) {
    }
    @GetMapping
    public ResponseEntity<List<SportCategory>> list(
    @RequestParam(value = "sportId", required = false) Long sportId,
    @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(sportCategoryService.findAll(sportId, activeOnly));
    }
    @GetMapping("/{id}")
    public ResponseEntity<SportCategory> details(
    @PathVariable Long id) {
        return ResponseEntity.ok(sportCategoryService.findById(id));
    }
    @PostMapping
    public ResponseEntity<?> create(
    @RequestBody SportCategoryRequest request) {
        try {
            return ResponseEntity.status(201) .body(sportCategoryService.create(toCategory(request), request.sportId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
    @PathVariable Long id,
    @RequestBody SportCategoryRequest request) {
        try {
            return ResponseEntity.ok(sportCategoryService.update(id, toCategory(request), request.sportId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
    @PathVariable Long id) {
        sportCategoryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Sport category deleted successfully"));
    }
    private SportCategory toCategory(SportCategoryRequest request) {
        SportCategory category = new SportCategory();
        category.setCode(request.code());
        category.setName(request.name());
        category.setDescription(request.description());
        category.setIsActive(request.isActive());
        category.setDisplayOrder(request.displayOrder());
        return category;
    }
}
