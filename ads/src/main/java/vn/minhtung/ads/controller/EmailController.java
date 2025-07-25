package vn.minhtung.ads.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdBudget;
import vn.minhtung.ads.repository.AdBudgetRepository;
import vn.minhtung.ads.repository.AdRepository;
import vn.minhtung.ads.service.EmailService;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;
    private final AdBudgetRepository adBudgetRepository;
    private final AdRepository adRepository;

    public EmailController(EmailService emailService, AdBudgetRepository adBudgetRepository,
            AdRepository adRepository) {
        this.emailService = emailService;
        this.adBudgetRepository = adBudgetRepository;
        this.adRepository = adRepository;
    }

    @PostMapping("/email/end-date")
    public ResponseEntity<String> sendAdExpireWarning(@RequestParam Long adId) throws IdInvalidException {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IdInvalidException("khong tim thay quoang cao "));

        if (ad.getEndDate() == null || ad.getEndDate().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("da het han su dung");
        }

        List<AdBudget> adBudgets = adBudgetRepository.findAllByAdId(adId);

        String email = ad.getUser().getEmail();
        emailService.sendAdBudgetEmail(email, ad, adBudgets, "sắp hết hạn");

        return ResponseEntity.ok().body("done");
    }
}
