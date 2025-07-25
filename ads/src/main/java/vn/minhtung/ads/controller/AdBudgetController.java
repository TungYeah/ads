package vn.minhtung.ads.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.minhtung.ads.domain.AdBudget;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.repository.AdBudgetRepository;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.service.AdBudgetService;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AdBudgetController {

    private final AdBudgetService adBudgetService;
    private final AdBudgetRepository adBudgetRepository;
    private final AdRepository adRepository;

    public AdBudgetController(AdBudgetService adBudgetService,
            AdBudgetRepository adBudgetRepository,
            AdRepository adRepository) {
        this.adBudgetService = adBudgetService;
        this.adBudgetRepository = adBudgetRepository;
        this.adRepository = adRepository;
    }

    @PostMapping("/ad-budgets")
    public ResponseEntity<AdBudget> crateAdBudgt(@Valid @RequestBody AdBudget adBudget) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.adBudgetService.creatAdBudget(adBudget));
    }

    @GetMapping("/ad-budgets")
    public ResponseEntity<ResultPageinationDTO> getAllAdBudgets(@Filter Specification<AdBudget> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.adBudgetService.getAllAdBudget(spec, pageable));
    }

    @GetMapping("/ad-budgets/{id}")
    public ResponseEntity<AdBudget> getAdBudgetById(@PathVariable long id) throws IdInvalidException {
        AdBudget adBudgetId = this.adBudgetService.getAdBudgetById(id);
        if (adBudgetId == null) {
            throw new IdInvalidException("Không tìm thấy id  ");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.adBudgetService.getAdBudgetById(id));
    }

    @PutMapping("/ad-budgets/{id}")
    public ResponseEntity<AdBudget> updateAdBudget(@PathVariable long id, @Valid @RequestBody AdBudget adBudget)
            throws IdInvalidException {
        return ResponseEntity.ok(adBudgetService.updateAdBudget(id, adBudget));
    }

    @DeleteMapping("/ad-budgets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        adBudgetService.deleteAdBudget(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/ad-budgets/total-cost")
    public ResponseEntity<BigDecimal> getTotalCostByAd(@RequestParam Long adId) throws IdInvalidException {
        BigDecimal total = adBudgetService.getTotalCostByAdId(adId);
        return ResponseEntity.ok().body(total);
    }

    @GetMapping("/ad-budgets/reamin")
    public ResponseEntity<BigDecimal> getRemainByAd(@RequestParam Long adId) throws IdInvalidException {
        BigDecimal remain = adBudgetService.getRemainBudget(adId);
        return ResponseEntity.ok().body(remain);
    }

    @GetMapping("ad-budgets/by-date")
    public ResponseEntity<List<AdBudget>> getBudgetsByDateRange(
            @RequestParam Long adId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        List<AdBudget> listAdBudgets = adBudgetRepository.findByAdIdAndDateBetween(adId, startDate, endDate);
        return ResponseEntity.ok().body(listAdBudgets);
    }
}
