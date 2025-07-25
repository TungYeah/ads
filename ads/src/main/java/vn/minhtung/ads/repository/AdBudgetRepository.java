package vn.minhtung.ads.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.minhtung.ads.domain.AdBudget;

@Repository
public interface AdBudgetRepository extends JpaRepository<AdBudget, Long>, JpaSpecificationExecutor<AdBudget> {
    List<AdBudget> findByAdId(long id);

    List<AdBudget> findAllByAdId(Long adId);

    List<AdBudget> findByAdIdAndDateBetween(Long adId, Instant startDate, Instant endDate);
}
