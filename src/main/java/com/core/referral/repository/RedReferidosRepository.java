package com.core.referral.repository;

import com.core.referral.entity.RedReferidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedReferidosRepository extends JpaRepository<RedReferidos, Long> {
}
