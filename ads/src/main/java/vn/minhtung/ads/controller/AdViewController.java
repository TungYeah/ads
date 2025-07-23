package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

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

    // @PostMapping("/ad-views")
    // public ResponseEntity<AdView> createAdView(AdView adView) throws
    // IdInvalidException{
    // AdView postAdView = this.adViewService.createAdView(adView);
    // return ResponseEntity.status(HttpStatus.CREATED).body(postAdView);
    // }

    @GetMapping("/ad-views")
    public ResponseEntity<ResultPageinationDTO> getAdViews(@Filter Specification spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.adViewService.getAllAdView(spec, pageable));
    }

    @GetMapping("/ad-views/count")
    public ResponseEntity<Long> countAdViewsByAdId(@RequestParam("adId") Long adId) {
        long count = adViewService.countByAdId(adId);
        return ResponseEntity.ok(count);
    }
}
