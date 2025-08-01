package vn.minhtung.ads.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdBudget;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.User;

import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.ad.CreateAdDTO;
import vn.minhtung.ads.domain.response.ad.ResAdById;
import vn.minhtung.ads.domain.response.ad.UpdateAdDTO;
import vn.minhtung.ads.mapper.AdMapper;
import vn.minhtung.ads.repository.AdBudgetRepository;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.repository.AdViewReposiotry;
import vn.minhtung.ads.repository.CategoryReposity;
import vn.minhtung.ads.repository.UserRepository;
import vn.minhtung.ads.util.PaginationUtil;
import vn.minhtung.ads.util.SecutiryUtil;
import vn.minhtung.ads.util.constant.StatusEnum;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Slf4j
@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryReposity categoryReposity;
    private final AdViewReposiotry adViewRepository;
    private final AdBudgetRepository adBudgetRepository;
    private final EmailService emailService;
    private final AdMapper adMapper;

    public AdService(AdRepository adRepository,
            UserRepository userRepository,
            CategoryReposity categoryReposity,
            AdViewReposiotry adViewRepository,
            AdBudgetRepository adBudgetRepository,
            EmailService emailService,
            AdMapper adMapper) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryReposity = categoryReposity;
        this.adViewRepository = adViewRepository;
        this.adBudgetRepository = adBudgetRepository;
        this.emailService = emailService;
        this.adMapper = adMapper;
    }

    private User getCurrentUser() throws IdInvalidException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IdInvalidException("Không xác thực được người dùng từ token");
        }
        String email = jwt.getSubject();
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "categories", key = "#name")
    public Category getCategoryByName(String name) throws IdInvalidException {
        return categoryReposity.findByName(name)
                .orElseThrow(() -> new IdInvalidException("Category not found"));
    }

    public CreateAdDTO handleAd(CreateAdDTO dto) throws IdInvalidException {
        User currentUser = getCurrentUser();
        Ad ad = adMapper.toEntity(dto);
        ad.setUser(currentUser);
        ad.setCategory(getCategoryByName(dto.getCategory()));
        Ad savedAd = adRepository.save(ad);
        log.info("Tạo mới quảng cáo ID: {}", savedAd.getId());
        return adMapper.toCreateAdDTO(savedAd);
    }

    public ResultPageinationDTO getAllAds(Specification<Ad> spec, Pageable pageable) {
        Page<Ad> ads = this.adRepository.findAll(spec, pageable);
        List<CreateAdDTO> adDTOs = adMapper.toCreateAdDTOs(ads.getContent());
        return PaginationUtil.build(ads, adDTOs);
    }

    @Cacheable(value = "ads", key = "#id")
    public Ad getAdById(long id) {
        log.debug("Tìm quảng cáo theo ID: {}", id);
        return this.adRepository.findById(id).orElseThrow();
    }

    public Ad updateAd(long id, UpdateAdDTO dto) throws IdInvalidException {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy quảng cáo ID: " + id));

        adMapper.updateAdFromDTO(dto, ad);

        if (dto.getCategory() != null) {
            Category category = categoryReposity.findByName(dto.getCategory())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy danh mục: " + dto.getCategory()));
            ad.setCategory(category);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            ad.setUpdatedBy(jwt.getSubject());
        }
        return adRepository.save(ad);
    }

    public void deleteAdById(long id) throws IdInvalidException {
        if (!adRepository.existsById(id)) {
            log.warn("Xóa thất bại. Không tồn tại quảng cáo ID: {}", id);
            throw new IdInvalidException("Không tìm thấy quảng cáo với ID " + id);
        }
        this.adRepository.deleteById(id);
        log.info("Đã xóa quảng cáo ID: {}", id);
    }

    public ResAdById convertToGetAdByIdDTO(Ad ad) {
        return adMapper.toResAdById(ad);
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
        log.debug("Ghi nhận lượt xem quảng cáo ID: {} từ user: {}", ad.getId(), user.getEmail());
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteExpiredAds() {
        Instant now = Instant.now();
        List<Ad> expiredAds = adRepository.findByEndDateBefore(now);
        for (Ad ad : expiredAds) {
            ad.setStatus(StatusEnum.ARCHIVED);
            log.info("Đánh dấu quảng cáo ID: {} là ARCHIVED", ad.getId());
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
            log.info("Gửi email cảnh báo hết hạn quảng cáo ID: {} cho user: {}", ad.getId(), ad.getUser().getEmail());
        }
    }
}
