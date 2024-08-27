package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Posthash;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashRepository extends JpaRepository<Posthash, Integer> {
}
