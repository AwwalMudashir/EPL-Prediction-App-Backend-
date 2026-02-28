package com.project.MyEplPredictor.repositories;

import com.project.MyEplPredictor.models.LeagueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueMemberRepo extends JpaRepository<LeagueMember,Long> {
}
