package vn.minhtung.ads.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.minhtung.ads.util.SecutiryUtil;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ads")
@Getter
@Setter
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ko dc de trong")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String targetUrl;

    private Instant startDate;
    private Instant endDate;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id")
    @JsonIgnore
    private Category category;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecutiryUtil.getCurrentUserLogin().isPresent() == true
                ? SecutiryUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecutiryUtil.getCurrentUserLogin()
                .orElse("");

        this.updatedAt = Instant.now();
    }
}
