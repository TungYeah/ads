package vn.minhtung.ads.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.adView.AdViewDTO;
import vn.minhtung.ads.mapper.AdViewMapper;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.repository.AdViewReposiotry;
import vn.minhtung.ads.repository.UserRepository;
import vn.minhtung.ads.util.PaginationUtil;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
public class AdViewService {

    private final AdViewReposiotry adViewReposiotry;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdViewMapper adViewMapper;

    public AdViewService(AdViewReposiotry adViewReposiotry,
            UserRepository userRepository,
            AdRepository adRepository,
            AdViewMapper adViewMapper) {
        this.adViewReposiotry = adViewReposiotry;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.adViewMapper = adViewMapper;
    }

    public AdView createAdView(AdView adView) throws IdInvalidException {
        long adId = adView.getAd().getId();
        long userId = adView.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User not found with id: " + userId));
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IdInvalidException("Ad not found with id: " + adId));

        adView.setUser(user);
        adView.setAd(ad);
        return adViewReposiotry.save(adView);
    }

    public ResultPageinationDTO getAllAdView(Specification<AdView> spec, Pageable pageable) {
        Page<AdView> adViews = this.adViewReposiotry.findAll(spec, pageable);
        List<AdViewDTO> listAdViews = adViewMapper.toDTOs(adViews.getContent());
        return PaginationUtil.build(adViews, listAdViews);
    }

    public long countByAdId(long adId) throws IdInvalidException{
        return adViewReposiotry.countByAdId(adId);
    }
}
