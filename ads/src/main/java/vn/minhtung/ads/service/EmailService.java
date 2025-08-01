package vn.minhtung.ads.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.mail.MailException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import vn.minhtung.ads.domain.Ad;
import vn.minhtung.ads.domain.AdBudget;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailService( JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null)
            return "0";
        return String.format("%,.0f", value);
    }

    @Async
    public void sendAdBudgetEmail(String to, Ad ad, List<AdBudget> adBudgets, String action) {
        Context context = new Context();

        BigDecimal budgetUsed = BigDecimal.ZERO;
        for (AdBudget b : adBudgets) {
            if (b.getCost() != null) {
                budgetUsed = budgetUsed.add(b.getCost());
            }
        }
        context.setVariable("userName", ad.getUser().getName());
        context.setVariable("adTitle", ad.getTitle());
        context.setVariable("action", action);
        context.setVariable("budgetTotal", formatCurrency(ad.getBudgetTotal()));
        context.setVariable("budgetUsed", formatCurrency(budgetUsed));
        context.setVariable("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String content = templateEngine.process("test-send-mail", context);
        this.sendEmailSync(to, "Thông báo quảng cáo", content, false, true);
    }

}
