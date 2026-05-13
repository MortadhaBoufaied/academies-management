package com.footballacademy.services.sport;

import com.footballacademy.model.Sport;
import com.footballacademy.repository.SportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class SportServiceTest {
    @Mock
    private SportRepository sportRepository;
    @InjectMocks
    private SportService sportService;
    private Sport football;
    private Sport basketball;
    @BeforeEach void setUp() {
        football = new Sport("FOOTBALL", "Football");
        football.setId(1L);
        football.setIsActive(true);
        football.setDisplayOrder(1);
        basketball = new Sport("BASKETBALL", "Basketball");
        basketball.setId(2L);
        basketball.setIsActive(true);
        basketball.setDisplayOrder(2);
    }
    @Test void getAllSports_ShouldReturnAllSports() {
        when(sportRepository.findAll()) .thenReturn(Arrays.asList(football, basketball));
        List<Sport> result = sportService.getAllSports();
        assertEquals(2, result.size());
        assertTrue(result.contains(football));
        assertTrue(result.contains(basketball));
        verify(sportRepository, times(1)) .findAll();
    }
    @Test void getActiveSports_ShouldReturnOnlyActiveSports() {
        football.setIsActive(true);
        basketball.setIsActive(false);
        when(sportRepository.findByIsActiveTrueOrderByDisplayOrderAsc()) .thenReturn(Arrays.asList(football));
        List<Sport> result = sportService.getActiveSports();
        assertEquals(1, result.size());
        assertTrue(result.contains(football));
        assertFalse(result.contains(basketball));
        verify(sportRepository, times(1)) .findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    @Test void getSportById_WhenExists_ShouldReturnSport() {
        when(sportRepository.findById(1L)) .thenReturn(Optional.of(football));
        Optional<Sport> result = sportService.getSportById(1L);
        assertTrue(result.isPresent());
        assertEquals(football, result.get());
        verify(sportRepository, times(1)) .findById(1L);
    }
    @Test void getSportById_WhenNotExists_ShouldReturnEmpty() {
        when(sportRepository.findById(999L)) .thenReturn(Optional.empty());
        Optional<Sport> result = sportService.getSportById(999L);
        assertFalse(result.isPresent());
        verify(sportRepository, times(1)) .findById(999L);
    }
    @Test void createSport_WhenValid_ShouldReturnCreatedSport() {
        Sport newSport = new Sport("TENNIS", "Tennis");
        when(sportRepository.existsByCode("TENNIS")) .thenReturn(false);
        when(sportRepository.save(any(Sport.
        class))) .thenReturn(newSport);
        Sport result = sportService.createSport(newSport);
        assertNotNull(result);
        assertEquals("TENNIS", result.getCode());
        verify(sportRepository, times(1)) .existsByCode("TENNIS");
        verify(sportRepository, times(1)) .save(newSport);
    }
    @Test void createSport_WhenCodeExists_ShouldThrowException() {
        when(sportRepository.existsByCode("FOOTBALL")) .thenReturn(true);
        assertThrows(IllegalArgumentException.
        class,() -> {
            sportService.createSport(football);
        });
        verify(sportRepository, times(1)) .existsByCode("FOOTBALL");
        verify(sportRepository, never()) .save(any(Sport.
        class));
    }
    @Test void updateSport_WhenValid_ShouldReturnUpdatedSport() {
        Sport updatedSport = new Sport("FOOTBALL", "Football Updated");
        when(sportRepository.findById(1L)) .thenReturn(Optional.of(football));
        when(sportRepository.existsByCode("FOOTBALL")) .thenReturn(true);
        when(sportRepository.save(any(Sport.
        class))) .thenReturn(football);
        Sport result = sportService.updateSport(1L, updatedSport);
        assertNotNull(result);
        verify(sportRepository, times(1)) .findById(1L);
        verify(sportRepository, times(1)) .save(any(Sport.
        class));
    }
    @Test void deleteSport_WhenExists_ShouldDeleteSport() {
        when(sportRepository.existsById(1L)) .thenReturn(true);
        doNothing() .when(sportRepository) .deleteById(1L);
        sportService.deleteSport(1L);
        verify(sportRepository, times(1)) .existsById(1L);
        verify(sportRepository, times(1)) .deleteById(1L);
    }
    @Test void deleteSport_WhenNotExists_ShouldThrowException() {
        when(sportRepository.existsById(999L)) .thenReturn(false);
        assertThrows(IllegalArgumentException.
        class,() -> {
            sportService.deleteSport(999L);
        });
        verify(sportRepository, times(1)) .existsById(999L);
        verify(sportRepository, never()) .deleteById(anyLong());
    }
    @Test void activateSport_ShouldSetIsActiveToTrue() {
        when(sportRepository.findById(1L)) .thenReturn(Optional.of(football));
        when(sportRepository.save(any(Sport.
        class))) .thenReturn(football);
        sportService.activateSport(1L);
        assertTrue(football.getIsActive());
        verify(sportRepository, times(1)) .findById(1L);
        verify(sportRepository, times(1)) .save(football);
    }
    @Test void deactivateSport_ShouldSetIsActiveToFalse() {
        when(sportRepository.findById(1L)) .thenReturn(Optional.of(football));
        when(sportRepository.save(any(Sport.
        class))) .thenReturn(football);
        sportService.deactivateSport(1L);
        assertFalse(football.getIsActive());
        verify(sportRepository, times(1)) .findById(1L);
        verify(sportRepository, times(1)) .save(football);
    }
}
