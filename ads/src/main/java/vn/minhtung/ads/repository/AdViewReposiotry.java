package vn.minhtung.ads.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.minhtung.ads.domain.AdView;

@Repository
public interface AdViewReposiotry extends JpaRepository<AdView, Long>, JpaSpecificationExecutor<AdView> {

    long countByAdId(long adId);

}
