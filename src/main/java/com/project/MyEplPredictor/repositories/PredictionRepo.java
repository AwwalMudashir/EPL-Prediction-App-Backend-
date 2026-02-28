package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionRepo extends JpaRepository<Prediction,Long> {
}
