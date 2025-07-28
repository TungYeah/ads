package vn.minhtung.ads.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.adView.AdViewDTO;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.repository.AdViewReposiotry;
import vn.minhtung.ads.repository.UserRepository;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
public class AdViewService {

    private final AdViewReposiotry adViewReposiotry;
    private final UserService userService;
    private final AdService adService;
    private final UserRepository userRepository;
    private final AdRepository adRepository;

    public AdViewService(AdViewReposiotry adViewReposiotry,
            UserService userService,
            AdService adService,
            UserRepository userRepository,
            AdRepository adRepository) {
        this.adViewReposiotry = adViewReposiotry;
        this.userService = userService;
        this.adService = adService;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
    }

    public AdView createAdView(AdView adView) throws IdInvalidException {

        long adId = adView.getAd().getId();
        long userId = adView.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User not found with id: "));
        Ad ad = adRepository.findById(adId).orElseThrow(() -> new IdInvalidException("Ad not found with id: "));
        adView.setUser(user);
        adView.setAd(ad);
        return adViewReposiotry.save(adView);
    }

    public ResultPageinationDTO getAllAdView(Specification<AdView> spec, Pageable pageable) {
        Page<AdView> adViews = this.adViewReposiotry.findAll(spec, pageable);
        ResultPageinationDTO rs = new ResultPageinationDTO();
        ResultPageinationDTO.Meta mt = new ResultPageinationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(adViews.getTotalPages());
        mt.setTotal(adViews.getTotalElements());

        rs.setMeta(mt);
        List<AdViewDTO> listAdViews = adViews.getContent()
                .stream().map(item -> new AdViewDTO(
                        item.getId(),
                        item.getUser().getId(),
                        item.getAd().getId(),
                        item.getViewedAt(),
                        item.getDeviceInfo(),
                        item.getIpAddress()))
                .collect(Collectors.toList());

        rs.setResult(listAdViews);
        return rs;
    }

    public long countByAdId(long adId) {
        return adViewReposiotry.countByAdId(adId);
    }

}
