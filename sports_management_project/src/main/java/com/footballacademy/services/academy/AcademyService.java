package com.footballacademy.services.academy;

import com.footballacademy.DTO.AcademyForm;
import com.footballacademy.model.Admin;
import com.footballacademy.model.Academy;
import com.footballacademy.model.Sport;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.AdminRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.auth.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public
class AcademyService {
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AuthService authService;
    private final AcademyAccessService academyAccessService;
    private final AcademySubscriptionService academySubscriptionService;
    private final PasswordEncoder passwordEncoder;
    public AcademyService(AcademyRepository academyRepository, SportRepository sportRepository, UserRepository userRepository, AdminRepository adminRepository, AuthService authService, AcademyAccessService academyAccessService, AcademySubscriptionService academySubscriptionService, PasswordEncoder passwordEncoder) {
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.authService = authService;
        this.academyAccessService = academyAccessService;
        this.academySubscriptionService = academySubscriptionService;
        this.passwordEncoder = passwordEncoder;
    }
    public List<Academy> findAll() {
        List<Academy> academies = academyRepository.findAll();
        academies.forEach(a -> {
            if (a.getSport() != null) {
                a.getSport().getId();
            }
        });
        return academies;
    }
    public Academy findById(Long id) {
        Academy academy = academyRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Academy not found: " + id));
        if (academy.getSport() != null) {
            academy.getSport().getId();
        }
        return academy;
    }
    public Academy create(Academy academy, List<Long> sportIds) {
        if (academy == null) {
            throw new IllegalArgumentException("Academy is required");
        } normalize(academy);
        if (academyRepository.existsBySlugIgnoreCase(academy.getSlug())) {
            throw new IllegalArgumentException("Academy slug already exists: " + academy.getSlug());
        } academy.setSport(resolvePrimarySport(sportIds));
        return academyRepository.save(academy);
    }
    public Academy createFromForm(AcademyForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Academy form is required");
        } Academy academy = form.toAcademy();
        normalize(academy);
        if (academy.getEmail() == null || academy.getEmail() .isBlank()) {
            throw new IllegalArgumentException("Academy email is required because it becomes the owner admin login");
        }
        if (form.getOwnerName() == null || form.getOwnerName() .isBlank()) {
            throw new IllegalArgumentException("Owner admin name is required");
        }
        if (form.getOwnerPassword() == null || form.getOwnerPassword() .isBlank()) {
            throw new IllegalArgumentException("Owner admin password is required");
        } ensureEmailAvailable(academy.getEmail(), null);
        if (academyRepository.existsBySlugIgnoreCase(academy.getSlug())) {
            throw new IllegalArgumentException("Academy slug already exists: " + academy.getSlug());
        } academy.setSport(resolvePrimarySport(form.getSportId() == null ? null : List.of(form.getSportId())));
        Academy savedAcademy = academyRepository.save(academy);
        User ownerUser = createOrUpdateOwnerUser(savedAcademy, form, null);
        savedAcademy.setOwnerUser(ownerUser);
        savedAcademy = academyRepository.save(savedAcademy);
        if (savedAcademy.getSubscriptionPaymentStatus() == Academy.SubscriptionPaymentStatus.PAID) {
            academySubscriptionService.syncSubscriptionState(savedAcademy, savedAcademy.getSubscriptionOffer(), Academy.SubscriptionPaymentStatus.PAID, "Subscription activated during academy creation");
        } else {
            academySubscriptionService.ensureInitialPayment(savedAcademy);
        } return savedAcademy;
    }
    public Academy update(Long id, Academy incoming, List<Long> sportIds) {
        Academy existing = findById(id);
        if (!academyAccessService.isSuperAdmin()) {
            academyAccessService.assertCanAccessAcademy(id);
        }
        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getSlug() != null && !incoming.getSlug() .isBlank()) {
            String newSlug = normalizeSlug(incoming.getSlug());
            if (!existing.getSlug() .equalsIgnoreCase(newSlug) && academyRepository.existsBySlugIgnoreCase(newSlug)) {
                throw new IllegalArgumentException("Academy slug already exists: " + newSlug);
            } existing.setSlug(newSlug);
        }
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setAddress(incoming.getAddress());
        existing.setCity(incoming.getCity());
        existing.setCountry(incoming.getCountry());
        existing.setLogoUrl(incoming.getLogoUrl());
        if (incoming.getStatus() != null) existing.setStatus(incoming.getStatus());
        if (sportIds != null) existing.setSport(resolvePrimarySport(sportIds));
        return academyRepository.save(existing);
    }
    public Academy updateFromForm(Long id, AcademyForm form) {
        Academy existing = findById(id);
        if (!academyAccessService.isSuperAdmin()) {
            academyAccessService.assertCanAccessAcademy(id);
        }
        if (form.getName() == null || form.getName() .isBlank()) {
            throw new IllegalArgumentException("Academy name is required");
        }
        User ownerUser = resolveOwnerUser(existing);
        if (form.getEmail() == null || form.getEmail() .isBlank()) {
            throw new IllegalArgumentException("Academy email is required because it stays linked to the owner admin login");
        }
        if (ownerUser == null &&(form.getOwnerName() == null || form.getOwnerName() .isBlank())) {
            throw new IllegalArgumentException("Owner admin name is required");
        }
        if (ownerUser == null &&(form.getOwnerPassword() == null || form.getOwnerPassword() .isBlank())) {
            throw new IllegalArgumentException("Owner admin password is required to bootstrap the owner account");
        } Academy previousSnapshot = new Academy();
        previousSnapshot.setSubscriptionOffer(existing.getSubscriptionOffer());
        previousSnapshot.setSubscriptionPaymentStatus(existing.getSubscriptionPaymentStatus());
        existing.setName(form.getName() .trim());
        String desiredSlug = form.getSlug() != null && !form.getSlug() .isBlank() ? normalizeSlug(form.getSlug()) : normalizeSlug(form.getName());
        if (!existing.getSlug() .equalsIgnoreCase(desiredSlug) && academyRepository.existsBySlugIgnoreCase(desiredSlug)) {
            throw new IllegalArgumentException("Academy slug already exists: " + desiredSlug);
        } existing.setSlug(desiredSlug);
        ensureEmailAvailable(form.getEmail(), ownerUser != null ? ownerUser.getId() : null);
        existing.setEmail(normalizeEmail(form.getEmail()));
        existing.setPhone(blankToNull(form.getPhone()));
        existing.setAddress(blankToNull(form.getAddress()));
        existing.setCity(blankToNull(form.getCity()));
        existing.setCountry(blankToNull(form.getCountry()));
        existing.setLogoUrl(blankToNull(form.getLogoUrl()));
        existing.setStatus(form.getStatus() != null ? form.getStatus() : existing.getStatus());
        existing.setSubscriptionOffer(form.getSubscriptionOffer() != null ? form.getSubscriptionOffer() : existing.getSubscriptionOffer());
        existing.setSubscriptionPaymentStatus(form.getSubscriptionPaymentStatus() != null ? form.getSubscriptionPaymentStatus() : existing.getSubscriptionPaymentStatus());
        existing.setSport(resolvePrimarySport(form.getSportId() == null ? null : List.of(form.getSportId())));
        Academy savedAcademy = academyRepository.save(existing);
        User savedOwner = createOrUpdateOwnerUser(savedAcademy, form, ownerUser);
        if (savedAcademy.getOwnerUser() == null || !savedAcademy.getOwnerUser() .getId() .equals(savedOwner.getId())) {
            savedAcademy.setOwnerUser(savedOwner);
            savedAcademy = academyRepository.save(savedAcademy);
        }
        if (previousSnapshot.getSubscriptionOffer() != savedAcademy.getSubscriptionOffer() || previousSnapshot.getSubscriptionPaymentStatus() != savedAcademy.getSubscriptionPaymentStatus()) {
            academySubscriptionService.syncSubscriptionState(savedAcademy, savedAcademy.getSubscriptionOffer(), savedAcademy.getSubscriptionPaymentStatus(), "Subscription updated from super admin academy form");
        }
        return savedAcademy;
    }
    public void delete(Long id) {
        Academy academy = findById(id);
        academyRepository.delete(academy);
    }
    public User createFirstAdmin(Long academyId, String name, String email, String password, String phone) {
        Academy academy = findById(academyId);
        List<User> admins = userRepository.findByAcademy_IdAndMainRole(academyId, User.UserRole.ADMIN);
        if (!admins.isEmpty()) {
            throw new IllegalArgumentException("Academy already has an ADMIN");
        } User user = new User();
        user.setNom(name);
        user.setEmail(email);
        user.setMdp(password);
        user.setTel(phone);
        user.setMainRole(User.UserRole.ADMIN);
        user.setAcademy(academy);
        return authService.register(user, academyId);
    }
    public Optional<User> findOwnerUser(Long academyId) {
        Academy academy = findById(academyId);
        User ownerUser = resolveOwnerUser(academy);
        return Optional.ofNullable(ownerUser);
    }
    private void normalize(Academy academy) {
        if (academy.getName() == null || academy.getName() .isBlank()) {
            throw new IllegalArgumentException("Academy name is required");
        }
        if (academy.getSlug() == null || academy.getSlug() .isBlank()) {
            academy.setSlug(normalizeSlug(academy.getName()));
        } else {
            academy.setSlug(normalizeSlug(academy.getSlug()));
        }
        academy.setEmail(normalizeEmail(academy.getEmail()));
        academy.setPhone(blankToNull(academy.getPhone()));
        academy.setAddress(blankToNull(academy.getAddress()));
        academy.setCity(blankToNull(academy.getCity()));
        academy.setCountry(blankToNull(academy.getCountry()));
        academy.setLogoUrl(blankToNull(academy.getLogoUrl()));
        if (academy.getStatus() == null) {
            academy.setStatus(Academy.AcademyStatus.ACTIVE);
        }
        if (academy.getSubscriptionOffer() == null) {
            academy.setSubscriptionOffer(Academy.SubscriptionOffer.REGULAR);
        }
        if (academy.getSubscriptionPaymentStatus() == null) {
            academy.setSubscriptionPaymentStatus(Academy.SubscriptionPaymentStatus.PENDING);
        }
    }
    private String normalizeSlug(String value) {
        String slug = value == null ? "" : value.trim() .toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("[^a-z0-9]+", "-") .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Academy slug is required");
        }
        return slug;
    }
    private Sport resolvePrimarySport(List<Long> sportIds) {
        if (sportIds == null) {
            return null;
        }
        for (Long sportId : sportIds) {
            if (sportId == null) continue;
            return sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + sportId));
        } return null;
    }
    private User createOrUpdateOwnerUser(Academy academy, AcademyForm form, User existingOwner) {
        String resolvedName = blankToNull(form.getOwnerName());
        String resolvedPhone = blankToNull(form.getOwnerPhone());
        if (existingOwner == null) {
            User user = new User();
            user.setNom(resolvedName);
            user.setEmail(academy.getEmail());
            user.setMdp(form.getOwnerPassword());
            user.setTel(resolvedPhone != null ? resolvedPhone : academy.getPhone());
            user.setMainRole(User.UserRole.ADMIN);
            user.setAcademy(academy);
            user.setActive(true);
            User savedUser = authService.register(user, academy.getId());
            promoteOwnerAdmin(savedUser, academy);
            return savedUser;
        } existingOwner.setNom(resolvedName != null ? resolvedName : existingOwner.getNom());
        existingOwner.setEmail(academy.getEmail());
        existingOwner.setTel(resolvedPhone != null ? resolvedPhone : blankToNull(existingOwner.getTel()));
        existingOwner.setAcademy(academy);
        existingOwner.setActive(true);
        if (form.getOwnerPassword() != null && !form.getOwnerPassword() .isBlank()) {
            existingOwner.setMdp(passwordEncoder.encode(form.getOwnerPassword()));
        } User savedUser = userRepository.save(existingOwner);
        promoteOwnerAdmin(savedUser, academy);
        return savedUser;
    }
    private void promoteOwnerAdmin(User user, Academy academy) {
        Admin admin = adminRepository.findByUser_Id(user.getId()) .orElseThrow(() -> new IllegalArgumentException("Admin profile not found for owner user: " + user.getId()));
        admin.setAcademy(academy);
        admin.setResponsibility(Admin.AdminResponsibility.ACADEMY_DIRECTOR);
        adminRepository.save(admin);
    }
    private User resolveOwnerUser(Academy academy) {
        if (academy.getOwnerUser() != null) {
            return academy.getOwnerUser();
        }
        if (academy.getEmail() != null && !academy.getEmail() .isBlank()) {
            Optional<User> emailOwner = userRepository.findByEmail(academy.getEmail()) .filter(user -> user.getMainRole() == User.UserRole.ADMIN) .filter(user -> user.getAcademyId() != null && user.getAcademyId() .equals(academy.getId()));
            if (emailOwner.isPresent()) {
                return emailOwner.get();
            }
        } return userRepository.findByAcademy_IdAndMainRole(academy.getId(), User.UserRole.ADMIN) .stream() .findFirst() .orElse(null);
    }
    private void ensureEmailAvailable(String email, Long existingUserId) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Academy email is required");
        } userRepository.findByEmail(normalizedEmail) .ifPresent(existingUser -> {
            if (existingUserId == null || !existingUserId.equals(existingUser.getId())) {
                throw new IllegalArgumentException("Email already exists: " + normalizedEmail);
            }
        });
    }
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        } return email.trim() .toLowerCase(Locale.ROOT);
    }
    private String blankToNull(String value) {
        if (value == null) {
            return null;
        } String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
