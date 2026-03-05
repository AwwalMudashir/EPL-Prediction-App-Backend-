package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.FixturesCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixturesCacheRepo extends JpaRepository<FixturesCache, Long> {
}
