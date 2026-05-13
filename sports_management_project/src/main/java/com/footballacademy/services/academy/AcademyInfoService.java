package com.footballacademy.services.academy;

import com.footballacademy.model.AcademyInfo;
import com.footballacademy.model.Division;
import com.footballacademy.model.Sport;
import com.footballacademy.repository.AcademyInfoRepository;
import com.footballacademy.repository.DivisionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public
class AcademyInfoService {
    private final AcademyInfoRepository academyInfoRepository;
    private final DivisionRepository divisionRepository;
    private final AcademyAccessService academyAccessService;
    public AcademyInfoService(AcademyInfoRepository academyInfoRepository, DivisionRepository divisionRepository, AcademyAccessService academyAccessService) {
        this.academyInfoRepository = academyInfoRepository;
        this.divisionRepository = divisionRepository;
        this.academyAccessService = academyAccessService;
    }
    /** Always return the singleton row (never null). */
    public AcademyInfo getAcademyInfo() {
        AcademyInfo info = requireSingleton();
        // Fix LazyInitializationException
        info.getDivisionsList() .size();
        return info;
    }
    /** Update all editable fields. */
    public AcademyInfo updateAcademyInfo(AcademyInfo incoming) {
        AcademyInfo info = requireSingleton();
        if (!academyAccessService.isSuperAdmin()) {
            info.setAcademy(academyAccessService.currentAcademyOrThrow());
        } info.setNom(incoming.getNom());
        info.setDescription(incoming.getDescription());
        info.setTotalPlayers(incoming.getTotalPlayers());
        info.setTotalCoaches(incoming.getTotalCoaches());
        info.setTopPlayers(incoming.getTopPlayers());
        info.setAchievements(incoming.getAchievements());
        info.setImageUrl(incoming.getImageUrl());
        /* CONTACT INFO */
        info.setEmail(incoming.getEmail());
        info.setPhone(incoming.getPhone());
        info.setFax(incoming.getFax());
        info.setWebsite(incoming.getWebsite());
        /* ADDRESS */
        info.setAddress(incoming.getAddress());
        info.setCity(incoming.getCity());
        info.setCountry(incoming.getCountry());
        info.setPostalCode(incoming.getPostalCode());
        info.setGoogleMapsUrl(incoming.getGoogleMapsUrl());
        /* SOCIAL MEDIA */
        info.setFacebook(incoming.getFacebook());
        info.setInstagram(incoming.getInstagram());
        info.setYoutube(incoming.getYoutube());
        info.setTiktok(incoming.getTiktok());
        /* EXTRA INFO */
        info.setFoundedYear(incoming.getFoundedYear());
        info.setSlogan(incoming.getSlogan());
        info.setMission(incoming.getMission());
        info.setVision(incoming.getVision());
        /* SUPPORT */
        info.setEmailSupport(incoming.getEmailSupport());
        info.setPhoneSupport(incoming.getPhoneSupport());
        /* DIVISIONS */
        if (incoming.getDivisionsList() != null) {
            List<Long> incomingDivisionIds = normalizeDivisionIds(incoming.getDivisionsList());
            if (!sameDivisionIds(info.getDivisionsList(), incomingDivisionIds)) {
                academyAccessService.assertCanManageAcademyDivisions();
                validateSelectableDivisions(incomingDivisionIds);
                info.setDivisionsList(incomingDivisionIds);
            }
        }
        if (info.getDivisionsList() == null) info.setDivisionsList(new ArrayList<>());
        return academyInfoRepository.save(info);
    }
    /** Add a division by ID. */
    public AcademyInfo addDivision(Long divisionId) {
        academyAccessService.assertCanManageAcademyDivisions();
        Division division = divisionRepository.findById(divisionId) .orElseThrow(() -> new RuntimeException("Division not found: " + divisionId));
        assertDivisionSelectableForCurrentAcademy(division);
        AcademyInfo info = requireSingleton();
        List<Long> ids = info.getDivisionsList();
        if (!ids.contains(divisionId)) ids.add(divisionId);
        academyInfoRepository.save(info);
        ids.size();
        // lazy load fix
        return info;
    }
    public void assertDivisionVisibleForCurrentAcademy(Division division) {
        academyAccessService.assertCanAccessDivision(division);
    }
    public boolean canManageAcademyDivisions() {
        return academyAccessService.canManageAcademyDivisions();
    }
    public String currentAdminResponsibilityName() {
        var responsibility = academyAccessService.currentAdminResponsibility();
        return responsibility != null ? responsibility.name() : null;
    }
    public List<Division> getAvailableDivisionsForCurrentAcademy() {
        if (academyAccessService.isSuperAdmin()) {
            return divisionRepository.findAll() .stream() .filter(this::isActiveDivision) .toList();
        } Sport sport = academyAccessService.currentAcademyOrThrow() .getSport();
        if (sport == null || sport.getId() == null) {
            return List.of();
        } return divisionRepository.findBySport_IdAndActiveTrueOrderByDisplayOrderAscNomAsc(sport.getId());
    }
    public List<Division> getAssociatedDivisionsForCurrentAcademy() {
        AcademyInfo info = getAcademyInfo();
        List<Division> divisions = new ArrayList<>();
        for (Long id : info.getDivisionsList()) {
            divisionRepository.findById(id) .filter(division -> {
                try {
                    academyAccessService.assertCanAccessDivision(division);
                    return true;
                } catch (RuntimeException ex) {
                    return false;
                }
            }) .ifPresent(divisions::add);
        } return divisions;
    }
    /** Remove division by ID. */
    public AcademyInfo removeDivision(Long divisionId) {
        academyAccessService.assertCanManageAcademyDivisions();
        AcademyInfo info = requireSingleton();
        List<Long> ids = info.getDivisionsList();
        ids.remove(divisionId);
        academyInfoRepository.save(info);
        ids.size();
        // lazy load fix
        return info;
    }
    /** Create or return singleton instance. */
    private AcademyInfo requireSingleton() {
        if (!academyAccessService.isSuperAdmin() && academyAccessService.currentUser() != null) {
            Long academyId = academyAccessService.currentAcademyOrThrow() .getId();
            return mergeDuplicateAcademyInfoRows(academyId) .orElseGet(() -> createDefaultAcademyInfo(true));
        } return academyInfoRepository.findTopByOrderByIdAsc() .orElseGet(() -> createDefaultAcademyInfo(false));
    }
    private java.util.Optional<AcademyInfo> mergeDuplicateAcademyInfoRows(Long academyId) {
        List<AcademyInfo> rows = academyInfoRepository.findByAcademy_IdOrderByIdAsc(academyId);
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        } AcademyInfo keeper = rows.get(0);
        keeper.getDivisionsList() .size();
        if (rows.size() == 1) {
            return java.util.Optional.of(keeper);
        } Set<Long> mergedDivisionIds = new LinkedHashSet<>(keeper.getDivisionsList());
        for (int i = 1;
        i < rows.size();
        i++) {
            AcademyInfo duplicate = rows.get(i);
            duplicate.getDivisionsList() .size();
            mergedDivisionIds.addAll(duplicate.getDivisionsList());
            academyInfoRepository.delete(duplicate);
        } keeper.setDivisionsList(new ArrayList<>(mergedDivisionIds));
        return java.util.Optional.of(academyInfoRepository.save(keeper));
    }
    private void validateSelectableDivisions(List<Long> divisionIds) {
        for (Long divisionId : divisionIds) {
            Division division = divisionRepository.findById(divisionId) .orElseThrow(() -> new RuntimeException("Division not found: " + divisionId));
            assertDivisionSelectableForCurrentAcademy(division);
        }
    }
    private void assertDivisionSelectableForCurrentAcademy(Division division) {
        if (academyAccessService.isSuperAdmin()) {
            return;
        } var academy = academyAccessService.currentAcademyOrThrow();
        Sport academySport = academy.getSport();
        Long academySportId = academySport != null ? academySport.getId() : null;
        Long divisionSportId = division.getSport() != null ? division.getSport() .getId() : null;
        if (academySportId == null || divisionSportId == null || !academySportId.equals(divisionSportId)) {
            throw new AccessDeniedException("This division is not available for the academy sport");
        }
        if (division.getAcademy() != null && !academyAccessService.canAccessAcademy(division.getAcademy())) {
            throw new AccessDeniedException("This division belongs to another academy");
        }
        if (!isActiveDivision(division)) {
            throw new AccessDeniedException("Inactive divisions cannot be associated to an academy");
        }
    }
    private boolean isActiveDivision(Division division) {
        return division != null && (division.getActive() == null || Boolean.TRUE.equals(division.getActive()));
    }
    private List<Long> normalizeDivisionIds(List<Long> divisionIds) {
        return new ArrayList<>(new LinkedHashSet<>(divisionIds == null ? List.of() : divisionIds));
    }
    private boolean sameDivisionIds(List<Long> existing, List<Long> incoming) {
        return new LinkedHashSet<>(existing == null ? List.of() : existing) .equals(new LinkedHashSet<>(incoming == null ? List.of() : incoming));
    }
    private AcademyInfo createDefaultAcademyInfo(boolean attachCurrentAcademy) {
        AcademyInfo n = new AcademyInfo();
        n.setNom("Football Academy");
        n.setDescription("");
        n.setTotalPlayers(0);
        n.setTotalCoaches(0);
        n.setTopPlayers("");
        n.setAchievements("");
        n.setImageUrl("");
        /* Default contact info */
        n.setEmail("");
        n.setPhone("");
        n.setFax("");
        n.setWebsite("");
        /* Address */
        n.setAddress("");
        n.setCity("");
        n.setCountry("");
        n.setPostalCode("");
        n.setGoogleMapsUrl("");
        /* Socials */
        n.setFacebook("");
        n.setInstagram("");
        n.setYoutube("");
        n.setTiktok("");
        /* Extra */
        n.setFoundedYear(null);
        n.setSlogan("");
        n.setMission("");
        n.setVision("");
        /* Support */
        n.setEmailSupport("");
        n.setPhoneSupport("");
        n.setDivisionsList(new ArrayList<>());
        if (attachCurrentAcademy) {
            n.setAcademy(academyAccessService.currentAcademyOrThrow());
        } return academyInfoRepository.save(n);
    }
}
