package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikesRepository extends JpaRepository<Likes, Integer> {
    List<Likes> findByPostid(int postid);

    Likes findByPostidAndLikenick(int postid, String likenick);
}
