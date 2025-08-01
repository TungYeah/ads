package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.ad.CreateAdDTO;
import vn.minhtung.ads.domain.response.ad.ResAdById;
import vn.minhtung.ads.domain.response.ad.UpdateAdDTO;
import vn.minhtung.ads.service.AdService;
import vn.minhtung.ads.mapper.AdMapper;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AdController {

    private final AdService adService;
    private final AdMapper adMapper;

    public AdController(AdService adService, AdMapper adMapper) {
        this.adService = adService;
        this.adMapper = adMapper;
    }

    @PostMapping("/ads")
    public ResponseEntity<CreateAdDTO> createAd(@Valid @RequestBody CreateAdDTO dto) throws IdInvalidException {
        CreateAdDTO responseDTO = this.adService.handleAd(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/ads")
    public ResponseEntity<ResultPageinationDTO> getAllAds(@Filter Specification<Ad> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.adService.getAllAds(spec, pageable));
    }

    @GetMapping("/ads/{id}")
    public ResponseEntity<ResAdById> getAdById(@PathVariable long id, HttpServletRequest request)
            throws IdInvalidException {
        Ad ad = this.adService.getAdById(id);
        if (ad == null) {
            throw new IdInvalidException("Không tìm thấy id quảng cáo: " + id);
        }
        adService.logAdView(ad, request);
        return ResponseEntity.status(HttpStatus.OK).body(this.adService.convertToGetAdByIdDTO(ad));
    }

    @PutMapping("/ads/{id}")
    public ResponseEntity<UpdateAdDTO> updateAd(@PathVariable long id, @RequestBody UpdateAdDTO dto)
            throws IdInvalidException {
        Ad updatedAd = adService.updateAd(id, dto);
        UpdateAdDTO response = adMapper.toUpdateAdDTO(updatedAd);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/ads/{id}")
    public ResponseEntity<Void> deleteAdById(@PathVariable("id") long id) throws IdInvalidException {
        Ad ad = this.adService.getAdById(id);
        if (ad == null) {
            throw new IdInvalidException("User khong ton tai Id" + id);
        }
        adService.deleteAdById(id);
        return ResponseEntity.ok().build();
    }
}
