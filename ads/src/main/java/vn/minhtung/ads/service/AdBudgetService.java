package vn.minhtung.ads.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdBudget;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.repository.AdBudgetRepository;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.util.PaginationUtil;
import vn.minhtung.ads.util.errors.IdInvalidException;

@Service
public class AdBudgetService {

    private final AdBudgetRepository adBudgetRepository;
    private final AdRepository adRepository;

    public AdBudgetService(AdBudgetRepository adBudgetRepository, AdRepository adRepository) {
        this.adBudgetRepository = adBudgetRepository;
        this.adRepository = adRepository;
    }

    public AdBudget creatAdBudget(AdBudget adBudget) throws IdInvalidException {
        long adId = adBudget.getAd().getId();
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IdInvalidException("khong thay d"));

        List<AdBudget> existingBudgets = adBudgetRepository.findByAdId(adId);
        BigDecimal totalCost = BigDecimal.ZERO;

        for (AdBudget b : existingBudgets) {
            if (b.getCost() != null) {
                totalCost = totalCost.add(b.getCost());
            }
        }

        BigDecimal budgetTotal = ad.getBudgetTotal();
        if (adBudget.getCost() != null) {
            totalCost = totalCost.add(adBudget.getCost());
        }
        if (totalCost.compareTo(budgetTotal) > 0) {
            BigDecimal remaining = budgetTotal
                    .subtract(totalCost.subtract(adBudget.getCost() != null ? adBudget.getCost() : BigDecimal.ZERO));
            throw new IdInvalidException("Tổng chi phí vượt quá ngân sách. Ngân sách còn lại: " + remaining);
        }

        adBudget.setAd(ad);
        return adBudgetRepository.save(adBudget);
    }

    public ResultPageinationDTO getAllAdBudget(Specification<AdBudget> spec, Pageable pageable) {
        Page<AdBudget> adBudgets = this.adBudgetRepository.findAll(spec, pageable);
        return PaginationUtil.build(adBudgets, adBudgets.getContent());
    }

    public AdBudget getAdBudgetById(long id) throws IdInvalidException {
        return this.adBudgetRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("khong tim thay id"));
    }

    public AdBudget updateAdBudget(Long id, AdBudget adBudget) throws IdInvalidException {
        AdBudget existing = adBudgetRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("khong thay id"));

        existing.setDate(adBudget.getDate());
        existing.setCost(adBudget.getCost());
        existing.setNote(adBudget.getNote());
        return adBudgetRepository.save(existing);
    }

    public void deleteAdBudget(long id) throws IdInvalidException {
        if (!adBudgetRepository.existsById(id)) {
            throw new IdInvalidException("khong tim thay id");
        }
        adBudgetRepository.deleteById(id);
    }

    public BigDecimal getTotalCostByAdId(Long adId) throws IdInvalidException {
        if (!adRepository.existsById(adId)) {
            throw new IdInvalidException("ko tim thay id ");
        }

        List<AdBudget> adBudgets = adBudgetRepository.findAllByAdId(adId);
        BigDecimal total = BigDecimal.ZERO;

        for (AdBudget budget : adBudgets) {
            if (budget.getCost() != null) {
                total = total.add(budget.getCost());
            }
        }
        return total;
    }

    public BigDecimal getRemainBudget(Long adId) throws IdInvalidException {
        Ad ad = this.adRepository.findById(adId).orElseThrow();
        List<AdBudget> list = adBudgetRepository.findAllByAdId(adId);
        BigDecimal used = BigDecimal.ZERO;
        for (AdBudget b : list) {
            if (b.getCost() != null) {
                used = used.add(b.getCost());
            }
        }

        BigDecimal remain = ad.getBudgetTotal().subtract(used);
        return remain;
    }
}
