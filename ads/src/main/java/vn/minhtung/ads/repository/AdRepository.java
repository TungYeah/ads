package vn.minhtung.ads.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.minhtung.ads.domain.Ad;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long>, JpaSpecificationExecutor<Ad> {

    List<Ad> findByEndDateBefore(Instant endDate);

    List<Ad> findByEndDateBetween(Instant startDate, Instant endDate);
}
