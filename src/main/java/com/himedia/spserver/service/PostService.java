package com.himedia.spserver.service;

import com.himedia.spserver.dao.*;
import com.himedia.spserver.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ImagesRepository imagesRepository;
    @Autowired
    private LikesRepository likesRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FollowRepository followRepository;


    public List<Post> getPostList(String word) {
        // return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        // return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id", "writer")); id 기준 먼저 내림차순 그다음 writer 기준 내림차순
        // return postRepository.findAll(Sort.by(Sort.Order.DESC("id"), Sort.Order.ASC("writer")));

        // 1. word 로 hashtag 테이블을 검색
        // 2. 검색 결과에 있는 tagid 들로 posthash 테이블에서 postid 들을 검색
        // 3. postid 들로 post 테이블에서 post 들을 검색
        // 1. select id from hashtag where word=?
        // 2. select postid from posthash where hashid=?
        // 3. select* from post where id=?
        List<Post> posts = null;
        if(word == null){
            posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }else{
            Optional<Hashtag> tagList = hashtagRepository.findByWord(word); // 1. word 로 hashtag 테이블을 검색
            if(tagList.isPresent()) { // 있는 단어라면 hashtag 테이블의 단어 id로 검색
                // 2. 검색 결과에 있는 tagid 들로 posthash 테이블에서 postid 들을 검색
                // 3. postid 들로 post 테이블에서 post 들을 검색
                // 1개의 메소드로 postRepository 서브쿼리를 이용하여 한꺼번에 적용
                // 2. select postid from posthash where hashid=?
                // 3. select* from post where id=?
                posts = postRepository.getPostListByTag(tagList.get().getId());
            }else{ // 검색어가 hashtag 테이블에 없는 단어라면 모두 검색
                posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            }
        }

        return posts;

    }

    public List<Images> getImages(int postid) {
        List<Images> imagesList = imagesRepository.findByPostid(postid);
        return imagesList;
    }

    public List<Likes> getLikes(int postid) {
        List<Likes> likesList = likesRepository.findByPostid(postid);
        return likesList;
    }

    public List<Reply> getReplyList(int postid) {
        List<Reply> replyList = replyRepository.findByPostidOrderByIdDesc(postid);
        return replyList;
    }

    public Likes addLike(int postid, String likenick) {
        Likes likes = likesRepository.findByPostidAndLikenick(postid, likenick);
        return likes;
    }

    public void addInsertLike(int postid, String likenick) {
        Likes likes = new Likes();
        likes.setPostid(postid);
        likes.setLikenick(likenick);
        likesRepository.save(likes);
    }

    public void addReply(Reply reply, int postid) {
        Reply reply1 = new Reply();
        reply1.setPostid(postid);
        reply1.setWriter(reply.getWriter());
        reply1.setContent(reply.getContent());
        replyRepository.save(reply1);
    }

    public void deleteReply(int id) {
        Optional<Reply> record = replyRepository.findById(id);
        if (record.isPresent()) {
            replyRepository.delete(record.get());
        }
    }

    public void deleteLike(int postid, String likenick) {
        Likes likes = likesRepository.findByPostidAndLikenick(postid, likenick);
        likesRepository.delete(likes);

    }

    @Autowired
    HashtagRepository hashtagRepository;
    @Autowired
    PostHashRepository postHashRepository;

    public Post insertPost(Post post) {
        Post post1 = postRepository.save(post); // post 테이블 데이터 삽입
        int postid = post1.getId(); // 방금 데이터 삽입한 것의 postid 호출
        String content = post.getContent();
        // 해시태그 #과 문자를 분리하기 위해 정규표현식 구현
        Matcher m = Pattern.compile("#([0-9a-zA-Z가-힣]*)").matcher(content); // 정규표현식으로 하나 호출
        // System.out.println("m");
        List<String> tags = new ArrayList<>();
        while(m.find()){
            // System.out.println(m.group(1)); // # 을 뺀 나머지 글자들을 취하기 위해 m.group(1) 을 출력
            tags.add(m.group(1));
        }
        int tagid = 0;
        for(String word : tags){
            Optional<Hashtag> record = hashtagRepository.findByWord(word); // hashtag 테이블에 태그가 있는지 검색
            if(!record.isPresent()){ // hashtag 가 태그가 존재하지 않는다면
                Hashtag hashtag = new Hashtag();
                hashtag.setWord(word);
                Hashtag htsave = hashtagRepository.save(hashtag); // hashtag 테이블에 태그 삽입
                tagid = htsave.getId(); // 방금 삽입한 테이블의 tagid 호출
            }else{ // hashtag 테이블에 검색한 태그가 존재한다면
                tagid = record.get().getId(); // 존재한 태그의 행의 id, 즉 tagid 호출
            }
            Posthash ph = new Posthash();
            ph.setPostid(postid);
            ph.setHashid(tagid);
            // 추출된 포스트 아이디와 태그 아이디로 PoshHash 테이블에 레코드 추가
            postHashRepository.save(ph); // 갖고온 postid와 tagid 로 posthash 테이블에 데이터 삽입
        }
        return post1;
    }

    public void insertImages(Images images) {
        imagesRepository.save(images);
    }

    public List<Post> getPostListByNickname(String nickname) {
       return postRepository.findByWriterOrderByIdDesc(nickname);
    }

    public List<Images> getImgListByPostid(int id) {
        return imagesRepository.findByPostid(id);
    }

    public void updateProfile(Member member) {
        Optional<Member> member1 = memberRepository.findByNickname(member.getNickname());

        if(member1.isPresent()) {
            Member member2 = member1.get();
            member2.setEmail(member.getEmail());
            member2.setNickname(member.getNickname());
            member2.setPwd(member.getPwd());
            member2.setPhone(member.getPhone());
            member2.setProfileimg(member.getProfileimg());
            member2.setProfilemsg(member.getProfilemsg());

            memberRepository.save(member2);
        }else{
            return;
        }

    }

    public Post getPost(int postid) {
        Optional<Post> post = postRepository.findById(postid);
        if(post.isPresent()) {
            return post.get();
        }else{
            return null;
        }
    }

    public List<Follow> getFollowings(String nickname) {
        return followRepository.findByFfrom(nickname);
    }

    public List<Follow> getFollowers(String nickname) {
        return followRepository.findByFto(nickname);
    }

    // ※ findBy.... findBy 뒤에 쓸 수 있는 명령 메소드들 ※
    // Distinct : findDistinctByName("scott"): 중복제거 검색
    // And : findByNameAndGender("scott", "gender") : 이름과 성별 동시 만족(and)
    // OR : findByNameOrGender("scott", "gender") : 이름이 만족하거나 성별이 만족(OR)
    // Is, Equals : findByName("scott"), findByNameIs("scott"), findByNameEquals("scott") : 값이 같음
    // Between : findByStartDateBetween(1,10) : 1과 10의 사이 값들 검색
    // LessTan : findByAgeLessThan(10) : 10보다 작은(<)
    // LessThanEqual : findByAgeLessTanEqual(10) : 10보다 작거나 같은(<=)
    // GreaterThan : findByAgeGreaterThanEqual(120) : 120보다 크거나 같은(>=)
    // After : findByStartDateAfter(날짜) : 날짜 이후
    // Before : findByStartDateBefore(날짜) : 날짜 이전
    // Like : findByNameLike("scott") 이름(scott)을 포함하는
    // StartingWith : findByNameStartingWith("scott") : 이름으로 시작하는
    // EndingWith : findByNameEndingWith("scott") : 이름으로 끝나는
    // Containing : findByNameContaining : 이름을 포함하는(Like 와 유사)
    // 평소에 사용하던 where name like "%철수%" 는 Containing 을 사용합니다.
    // Like 를 사용하던 where name like "철수" 와 같이 동작하므로 결과가 없을 수도 있습니다.
    // OrderBy : findByAgeOrderByIdDesc() : Age 필드와 같은 행을 찾되, id 필드 기준 내림차순 정렬
    // In : findByAgeIn(Collection<Age> ages) : In 함수 사용
    // true : findByActiveTrue() : active 필드값이 true
    // false : findByActiveFalse() : active 필드값이 false
    // IgnoreCase : findByNameIgnoreCase("scott") 이름을 검색하되 대소문자 구분하지 않음
}
