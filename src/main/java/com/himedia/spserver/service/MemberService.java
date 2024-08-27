package com.himedia.spserver.service;

import com.himedia.spserver.dao.FollowRepository;
import com.himedia.spserver.dao.MemberRepository;
import com.himedia.spserver.entity.Follow;
import com.himedia.spserver.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FollowRepository followRepository;

    public Member getMember(String email) {
        Optional<Member> member = memberRepository.findByEmail(email); // 단하나의 데이터를 찾을 때 없을 수도 있기에 Optional<Member> 를 이용
        if(!member.isPresent()) {
            // isPresent() : 해당 객체가 인스턴스를 저장하고 있다면 true, null 이면 false 를 리턴
            // isEmpty() : isPresent() 의 반대값을 리턴합니다.
            return null;
        }else{
            // Optional 내부 객체를 꺼내서 리턴합니다 그때 get()을 사용합니다.
            return member.get();
        }
    }

    public List<Follow> getFollowings(String nickname) {
        List<Follow> followingList = followRepository.findByFfrom(nickname);
        return followingList;
    }

    public List<Follow> getFollowers(String nickname) {
        List<Follow> followerList = followRepository.findByFto(nickname);
        return followerList;
    }

    public Member getMemberBySnsid(String id) {
        Optional<Member> member = memberRepository.findBySnsid(id);
        if(!member.isPresent()) {
            // isPresent() : 해당 객체가 인스턴스를 저장하고 있다면 true, null 이면 false 를 리턴
            // isEmpty() : isPresent() 의 반대값을 리턴합니다.
            return null;
        }else{
            // Optional 내부 객체를 꺼내서 리턴합니다 그때 get()을 사용합니다.
            return member.get();
        }
    }

    public void insertMember(Member member) {
        memberRepository.save(member);
    }

    public Member getFindEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if(!member.isPresent()) {
            // isPresent() : 해당 객체가 인스턴스를 저장하고 있다면 true, null 이면 false 를 리턴
            // isEmpty() : isPresent() 의 반대값을 리턴합니다.
            return null;
        }else{
            // Optional 내부 객체를 꺼내서 리턴합니다 그때 get()을 사용합니다.
            return member.get();
        }
    }

    public Member getFindNickName(String nickname) {
        Optional<Member> member = memberRepository.findByNickname(nickname);
        if(!member.isPresent()) {
            // isPresent() : 해당 객체가 인스턴스를 저장하고 있다면 true, null 이면 false 를 리턴
            // isEmpty() : isPresent() 의 반대값을 리턴합니다.
            return null;
        }else{
            // Optional 내부 객체를 꺼내서 리턴합니다 그때 get()을 사용합니다.
            return member.get();
        }
    }


    public void onFollow(String ffrom, String fto) {
        // ffrom 과 fto 로 전달된 값으로 레코드가 있는지 검사
        Optional<Follow> record = followRepository.findByFfromAndFto(ffrom, fto);
        if(!record.isPresent()) { // 없다면
            Follow follow = new Follow();
            follow.setFfrom(ffrom);
            follow.setFto(fto);
            followRepository.save(follow);
        }
    }

    public void onUnFollow(String ffrom, String fto) {
        // ffrom 과 fto 로 전달된 값으로 레코드가 있는지 검사
        Optional<Follow> record = followRepository.findByFfromAndFto(ffrom, fto);
        if(record.isPresent()) { // 있다면
            followRepository.deleteById(record.get().getId());
        }
    }
}
