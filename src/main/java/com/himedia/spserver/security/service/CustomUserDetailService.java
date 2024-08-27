package com.himedia.spserver.security.service;

import com.himedia.spserver.dao.MemberRepository;
import com.himedia.spserver.dto.MemberDTO;
import com.himedia.spserver.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // loadUserByUsername 역할은 전에 사용하던 getMember 메서드의 역할
        log.info("-----------------------loadUserByUsername------------------" + username);

        // 멤버 조회
        Member member = memberRepository.getWithRoles(username);
        if(member == null){ // 없으면 Not Found 처리
            throw new UsernameNotFoundException(username + " - User Not found");
        }

        // 존재하면 로그인 처리를 위해 Entity 데이터를 DTO 데이터로 옮김
        MemberDTO memberDTO = new MemberDTO(
                member.getNickname(), member.getPwd(), member.getEmail(), member.getProvider(), member.getPhone(), member.getSnsid(), member.getProfileimg(), member.getProfilemsg(), member.getMemberRoleList().stream().map(memberRole -> memberRole.name()).collect(Collectors.toList())

        );
        log.info(memberDTO);
        log.info(member);
        return memberDTO;
    }
}
