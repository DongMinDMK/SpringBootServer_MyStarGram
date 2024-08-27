package com.himedia.spserver.security.filter;

import com.google.gson.Gson;
import com.himedia.spserver.dto.MemberDTO;
import com.himedia.spserver.security.util.CustomJWTException;
import com.himedia.spserver.security.util.JWTUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeaderStr = request.getHeader("Authorization");
        try {
            //Bearer accestoken...
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);
            log.info("JWT claims: " + claims);

            String email = (String) claims.get("email");
            String pwd = (String) claims.get("pwd");
            String nickname = (String) claims.get("nickname");
            String snsid = (String) claims.get("snsid");
            String provider = (String) claims.get("provider");
            String profileimg = (String) claims.get("profileimg");
            String phone = (String) claims.get("phone");
            String profilemsg = (String) claims.get("profilemsg");

            List<String> roleNames = (List<String>) claims.get("roleNames");
            MemberDTO memberDTO = new MemberDTO( nickname, pwd, email, provider, phone, snsid,
                     profileimg, profilemsg, roleNames);
            log.info("-----------------------------------");
            log.info(memberDTO);
            log.info(memberDTO.getAuthorities()); // 권한 추출

            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberDTO, pwd , memberDTO.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        }catch(Exception | CustomJWTException e){
            log.error("JWT Check Error..............");
            log.error(e.getMessage());
            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)  throws ServletException{
        String path = request.getRequestURI();
        log.info("check uri.............." + path);

        if(request.getMethod().equals("OPTIONS"))
            return true;

        if(path.startsWith("/members/loginLocal"))
            return true;

        if(path.startsWith("/images"))
            return true;

        if(path.startsWith("/uploads"))
            return true;

        if(path.startsWith("/members/join"))
            return true;

        if(path.startsWith("/members/emailCheck"))
            return true;

        if(path.startsWith("/members/nickNameCheck"))
            return true;

        if(path.startsWith("/members/fileupload"))
            return true;
        if(path.startsWith("/members/kakaostart"))
            return true;
        if(path.startsWith("/members/kakaoLogin"))
            return true;
        if(path.startsWith("/favicon.ico"))
            return true;

        return false;

    }
}
