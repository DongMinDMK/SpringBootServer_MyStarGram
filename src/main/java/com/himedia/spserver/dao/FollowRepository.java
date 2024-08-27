package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    List<Follow> findByFfrom(String nickname);
    // [{follow_from:"hong1", follow_to:"abcd"}, {follow_from:"hong1", follow_to:"cdef"}...]
    List<Follow> findByFto(String nickname);

    Optional<Follow> findByFfromAndFto(String ffrom, String fto);
    // [{follow_from:"abcd", follow_to:"hong1"}, {follow_from:"cdef", follow_to:"hong1"}...]
}
