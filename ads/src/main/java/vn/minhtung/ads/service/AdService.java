package vn.minhtung.ads.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdBudget;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.User;

import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO.Meta;
import vn.minhtung.ads.domain.response.ad.CreateAdDTO;
import vn.minhtung.ads.domain.response.ad.ResAdById;
import vn.minhtung.ads.repository.AdBudgetRepository;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.repository.AdViewReposiotry;
import vn.minhtung.ads.repository.CategoryReposity;
import vn.minhtung.ads.repository.UserRepository;
import vn.minhtung.ads.util.SecutiryUtil;
import vn.minhtung.ads.util.constant.StatusEnum;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryReposity categoryReposity;
    private final AdViewReposiotry adViewRepository;
    private final AdBudgetRepository adBudgetRepository;
    private final EmailService emailService;

    public AdService(AdRepository adRepository,
            UserRepository userRepository,
            CategoryReposity categoryReposity,
            AdViewReposiotry adViewRepository,
            AdBudgetRepository adBudgetRepository,
            EmailService emailService) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryReposity = categoryReposity;
        this.adViewRepository = adViewRepository;
        this.adBudgetRepository = adBudgetRepository;
        this.emailService = emailService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("Không xác thực được người dùng từ token");
        }
        String email = jwt.getSubject();
        return userRepository.findByEmail(email);
    }

    public CreateAdDTO handleAd(CreateAdDTO dto) {
        User currentUser = getCurrentUser();
        Ad ad = new Ad();
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setImageUrl(dto.getImageUrl());
        ad.setTargetUrl(dto.getTargetUrl());
        ad.setStatus(dto.getStatus());
        ad.setBudgetTotal(dto.getBudgetTotal());
        ad.setStartDate(dto.getStartDate());
        ad.setEndDate(dto.getEndDate());
        ad.setUser(currentUser);
        Category category = categoryReposity.findByName(dto.getCategory())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ad.setCategory(category);
        Ad savedAd = adRepository.save(ad);
        return convertAdDTO(savedAd);
    }

    public ResultPageinationDTO getAllAds(Specification<Ad> spec, Pageable pageable) {
        Page<Ad> ads = this.adRepository.findAll(spec, pageable);

        List<CreateAdDTO> adDTOs = ads.stream()
                .map(this::convertAdDTO)
                .toList();

        Meta mt = new Meta();
        mt.setPage(ads.getNumber() + 1);
        mt.setPageSize(ads.getSize());
        mt.setPages(ads.getTotalPages());
        mt.setTotal(ads.getTotalElements());

        ResultPageinationDTO rs = new ResultPageinationDTO();
        rs.setMeta(mt);
        rs.setResult(adDTOs);
        return rs;
    }

    @Cacheable(value = "ads", key = "#id")
    public Ad getAdById(long id) {
        return this.adRepository.findById(id).orElseThrow();
    }

    public Ad updateAd(Ad ad) {
        Optional<Ad> updateAd = this.adRepository.findById(ad.getId());
        if (updateAd.isPresent()) {
            Ad currentAd = updateAd.get();
            currentAd.setTitle(ad.getTitle());
            currentAd.setDescription(ad.getDescription());
            currentAd.setImageUrl(ad.getImageUrl());
            currentAd.setTargetUrl(ad.getTargetUrl());
            currentAd.setStartDate(ad.getStartDate());
            currentAd.setEndDate(ad.getEndDate());
            return this.adRepository.save(currentAd);
        }
        return null;
    }

    public void deleteAdById(long id) {
        if (!adRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy quảng cáo với ID " + id);
        }
        this.adRepository.deleteById(id);
    }

    public CreateAdDTO convertAdDTO(Ad ad) {
        CreateAdDTO createAdDTO = new CreateAdDTO();
        createAdDTO.setId(ad.getId());
        createAdDTO.setTitle(ad.getTitle());
        createAdDTO.setDescription(ad.getDescription());
        createAdDTO.setImageUrl(ad.getImageUrl());
        createAdDTO.setTargetUrl(ad.getTargetUrl());
        createAdDTO.setStartDate(ad.getStartDate());
        createAdDTO.setEndDate(ad.getEndDate());

        if (ad.getCategory() != null) {
            createAdDTO.setCategory(ad.getCategory().getName());
        }
        return createAdDTO;
    }

    public ResAdById convertToGetAdByIdDTO(Ad ad) {
        ResAdById getAdByIdDTO = new ResAdById();
        getAdByIdDTO.setId(ad.getId());
        getAdByIdDTO.setTitle(ad.getTitle());
        getAdByIdDTO.setDescription(ad.getDescription());
        getAdByIdDTO.setImageUrl(ad.getImageUrl());
        getAdByIdDTO.setTargetUrl(ad.getTargetUrl());
        getAdByIdDTO.setStartDate(ad.getStartDate());
        getAdByIdDTO.setEndDate(ad.getEndDate());
        getAdByIdDTO.setCategory(ad.getCategory().getName());
        return getAdByIdDTO;
    }

    public void deleteAd(long id) {
        if (!adRepository.existsById(id)) {
            throw new NoSuchElementException("Ad not found");
        }
        this.adRepository.deleteById(id);
    }

    public void logAdView(Ad ad, HttpServletRequest request) throws IdInvalidException {
        Optional<String> emailOpt = SecutiryUtil.getCurrentUserLogin();
        if (emailOpt.isEmpty())
            return;

        String email = emailOpt.get();
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("ko tim thay email");

        AdView adView = new AdView();
        adView.setAd(ad);
        adView.setUser(user);
        adView.setViewedAt(Instant.now());
        adView.setDeviceInfo(request.getHeader("User-Agent"));
        adView.setIpAddress(request.getRemoteAddr());

        adViewRepository.save(adView);
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteExpiredAds() {
        Instant now = Instant.now();
        List<Ad> expiredAds = adRepository.findByEndDateBefore(now);
        for (Ad ad : expiredAds) {
            ad.setStatus(StatusEnum.ARCHIVED);
        }

        adRepository.saveAll(expiredAds);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void notifyAdExpiringSoon() {
        Instant now = Instant.now();
        Instant nextDay = now.plus(1, ChronoUnit.DAYS);

        List<Ad> adsExpiringSoon = adRepository.findByEndDateBetween(now, nextDay);
        for (Ad ad : adsExpiringSoon) {
            List<AdBudget> adBudgets = adBudgetRepository.findAllByAdId(ad.getId());
            emailService.sendAdBudgetEmail(ad.getUser().getEmail(), ad, adBudgets, "sắp hết hạn");
        }
    }

}
