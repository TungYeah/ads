package vn.minhtung.ads.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.User;

import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO.Meta;
import vn.minhtung.ads.domain.response.ad.CreateAdDTO;
import vn.minhtung.ads.domain.response.ad.ResAdById;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.repository.CategoryReposity;
import vn.minhtung.ads.repository.UserRepository;

@Service
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryReposity categoryReposity;

    public AdService(AdRepository adRepository, UserRepository userRepository, CategoryReposity categoryReposity) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryReposity = categoryReposity;
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
}
