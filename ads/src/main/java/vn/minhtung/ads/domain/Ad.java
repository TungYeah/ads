package vn.minhtung.ads.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.minhtung.ads.util.SecutiryUtil;
import vn.minhtung.ads.util.constant.StatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    private StatusEnum status;

    private BigDecimal budgetTotal;

    private Instant startDate;
    private Instant endDate;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id")
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<AdView> adViews = new ArrayList<>();

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
