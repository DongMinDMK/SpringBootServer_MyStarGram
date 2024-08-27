package com.himedia.spserver.dao;


import com.himedia.spserver.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
     Optional<Member> findByEmail(String email);

     Optional<Member> findBySnsid(String id);

     Optional<Member> findByNickname(String nickname);

     // 권한을 같이 조회 - 쿼리가 한번 날라감.
     @EntityGraph(attributePaths = {"memberRoleList"}) // 이렇게 하면 데이터를 권한과 함께 부여
     @Query("select m from Member m where m.nickname =:nickname")
     Member getWithRoles(@Param("nickname") String nickname);
}
