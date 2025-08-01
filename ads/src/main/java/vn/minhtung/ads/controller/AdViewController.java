package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.minhtung.ads.domain.AdView;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.service.AdViewService;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AdViewController {

    private final AdViewService adViewService;

    public AdViewController(AdViewService adViewService) {
        this.adViewService = adViewService;
    }

    @PostMapping("/ad-views")
    public ResponseEntity<AdView> createAdView(@Valid @RequestBody AdView adView) throws IdInvalidException {
        AdView postAdView = this.adViewService.createAdView(adView);
        return ResponseEntity.status(HttpStatus.CREATED).body(postAdView);
    }

    @GetMapping("/ad-views")
    public ResponseEntity<ResultPageinationDTO> getAdViews(
            @Filter Specification<AdView> spec,
            Pageable pageable) {
        return ResponseEntity.ok(this.adViewService.getAllAdView(spec, pageable));
    }

    @GetMapping("/ad-views/count")
    public ResponseEntity<Long> countAdViewsByAdId(@RequestParam("adId") Long adId) {
        try {
            long count = adViewService.countByAdId(adId);
            return ResponseEntity.ok(count);
        } catch (IdInvalidException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
