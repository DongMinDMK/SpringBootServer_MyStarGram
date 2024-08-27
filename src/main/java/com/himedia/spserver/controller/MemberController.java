package com.himedia.spserver.controller;

import com.google.gson.Gson;
import com.himedia.spserver.dto.KakaoProfile;
import com.himedia.spserver.dto.OAuthToken;
import com.himedia.spserver.entity.Follow;
import com.himedia.spserver.entity.Member;
import com.himedia.spserver.security.CustomSecurityConfig;
import com.himedia.spserver.security.util.CustomJWTException;
import com.himedia.spserver.security.util.JWTUtil;
import com.himedia.spserver.service.MemberService;
import com.himedia.spserver.service.PostService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;
    @Autowired
    private PostService postService;

//    @PostMapping("/loginLocal")
//    public HashMap<String, Object> loginLocal(@RequestBody Member member, HttpServletRequest request) {
//        HashMap<String, Object> result = new HashMap<>();
//
//        Member member1 = memberService.getMember(member.getEmail());
//
//        if(member1 == null) {
//            result.put("message", "해당 이메일이 존재하지 않습니다.");
//        }else if(!member1.getPwd().equals(member.getPwd())){
//            result.put("message", "비밀번호가 일치하지 않습니다.");
//        }else{
//            HttpSession session = request.getSession();
//            session.setAttribute("loginUser", member1);
//            result.put("message", "OK");
//        }
//
//        return result;
//    }

    @GetMapping("/test")
    public String test(){
        return "AWS SpringBoot Service Test3";
    }

    @GetMapping("/getLoginUser")
    public HashMap<String, Object> getLoginUser(HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<>();
        HttpSession session = request.getSession();
        Member loginUser = (Member) session.getAttribute("loginUser");
        List<Follow> followingList = memberService.getFollowings(loginUser.getNickname());
        List<Follow> followersList = memberService.getFollowers(loginUser.getNickname());
        result.put("loginUser", loginUser);
        // result.put("followings", followingList);
        // result.put("followers", followersList);

        return result;
    }

    @Value("${kakao.client_id}")
    private String client_id;
    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @RequestMapping("/kakaostart")
    public @ResponseBody String kakaostart(){
        String a = "<script type='text/javascript'>"
                + "location.href='https://kauth.kakao.com/ouath/authorize?"
                + "client_id=" + client_id + "&"
                + "redirect_uri=" + redirect_uri + "&"
                + "response_type=code';" + "</script>";
        return a;
    }

    @RequestMapping("/kakaoLogin")
    public void loginKakao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String endpoint = "https://kauth.kakao.com/oauth/token";
        URL url = new URL(endpoint); // import java.net.URL;
        String bodyData = "grant_type=authorization_code&";
        bodyData += "client_id=" + client_id + "&";
        bodyData += "redirect_uri=" + redirect_uri + "&";
        bodyData += "code=" + code;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // import java.net.HttpURLConnection;
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        conn.setDoOutput(true);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
        bw.write(bodyData);
        bw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String input = "";
        StringBuilder sb = new StringBuilder(); // 조각난 String 을 조립하기위한 객체
        while ((input = br.readLine()) != null) {
            sb.append(input);
            //System.out.println(input); // 수신된 토큰을 콘솔에 출력합니다
        }
        Gson gson = new Gson();
        OAuthToken oAuthToken = gson.fromJson(sb.toString(), OAuthToken.class);
        String endpoint2 = "https://kapi.kakao.com/v2/user/me";
        URL url2 = new URL(endpoint2);

        HttpsURLConnection conn2 = (HttpsURLConnection) url2.openConnection();
        conn2.setRequestProperty("Authorization", "Bearer " + oAuthToken.getAccess_token());
        conn2.setDoOutput(true);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(conn2.getInputStream(), "UTF-8"));
        String input2 = "";
        StringBuilder sb2 = new StringBuilder();
        while ((input2 = br2.readLine()) != null) {
            sb2.append(input2);
            //System.out.println(input2);
        }
        Gson gson2 = new Gson();
        KakaoProfile kakaoProfile = gson2.fromJson(sb2.toString(), KakaoProfile.class);
        KakaoProfile.KakaoAccount ac = kakaoProfile.getAccount();
        KakaoProfile.KakaoAccount.Profile pf = ac.getProfile();
        System.out.println("id : " + kakaoProfile.getId());
        System.out.println("KakaoAccount-Email : " + ac.getEmail());
        System.out.println("Profile-Nickname : " + pf.getNickname());

        Member member = memberService.getMemberBySnsid( kakaoProfile.getId() );
        if( member == null) {
            member = new Member();
           //  member.setEmail( pf.getNickname() );
            member.setEmail( pf.getNickname() );  // 전송된 이메일이 없으면 pf.getNickname()
            member.setNickname( pf.getNickname() );
            member.setProvider( "KAKAO" );
            PasswordEncoder passwordEncoder = customSecurityConfig.passwordEncoder(); //비밀번호 암호화 도구(bCrypt)
            member.setPwd( passwordEncoder.encode("KAKAO") );
            member.setSnsid(kakaoProfile.getId());

            memberService.insertMember(member);
        }
//        HttpSession session = request.getSession();
//        session.setAttribute("loginUser", member);
        String nickname = URLEncoder.encode(pf.getNickname(), "UTF-8");
        response.sendRedirect("http://localhost:3000/kakaosaveinfo/" + nickname);
    }

    @GetMapping("/logout")
    public HashMap<String, Object> logout(HttpServletRequest request){
        HashMap<String, Object> result = new HashMap<>();
        HttpSession session = request.getSession();
        session.removeAttribute("loginUser");

        result.put("message", "OK");
        return result;
    }

    @PostMapping("/emailCheck")
    public HashMap<String, Object> emailCheck(@RequestParam("email") String email){
        HashMap<String, Object> result = new HashMap<>();

        Member member1 = memberService.getFindEmail(email);

        if(member1 != null){
            result.put("message", "NO");
        }else{
            result.put("message", "OK");
        }
        return result;
    }

    @PostMapping("/nickNameCheck")
    public HashMap<String, Object> nickNameCheck(@RequestParam("nickname") String nickname){
        HashMap<String, Object> result = new HashMap<>();

        Member member1 = memberService.getFindNickName(nickname);

        if(member1 != null){
            result.put("message", "NO");
        }else{
            result.put("message", "OK");
        }
        return result;
    }

    @Autowired
    CustomSecurityConfig customSecurityConfig;

    @PostMapping("/join")
    public HashMap<String, Object> join(@RequestBody Member member){
        HashMap<String, Object> result = new HashMap<>();

        PasswordEncoder pe = customSecurityConfig.passwordEncoder();
        member.setPwd(pe.encode(member.getPwd()));

        memberService.insertMember(member);
        result.put("message", "OK");

        return result;
    }

    @Autowired
    ServletContext servletContext;

    @PostMapping("/fileupload")
    public HashMap<String, Object> fileUpload(@RequestParam("image") MultipartFile file){
        HashMap<String, Object> result = new HashMap<>();

        String path = servletContext.getRealPath("/uploads");

        Calendar today = Calendar.getInstance();
        long dt = today.getTimeInMillis();

        String filename = file.getOriginalFilename();
        String fn1 = filename.substring(0, filename.indexOf("."));
        String fn2 = filename.substring(filename.indexOf("."));
        String uploadPath = path + "/" + fn1 + dt + fn2;

        try{
            file.transferTo(new File(uploadPath));
            // result.put("image", filename);
            result.put("filename", fn1 + dt + fn2);
        }catch(IllegalStateException | IOException e){
            e.printStackTrace();
        }

        return result;
    }

    @PostMapping("/follow")
    public HashMap<String, Object> follow(@RequestParam("ffrom") String ffrom, @RequestParam("fto") String fto){
        HashMap<String, Object> result = new HashMap<>();

        memberService.onFollow(ffrom, fto);
        result.put("message", "OK");


        return result;
    }

    @PostMapping("/unfollow")
    public HashMap<String, Object> unfollow(@RequestParam("ffrom") String ffrom, @RequestParam("fto") String fto){
        HashMap<String, Object> result = new HashMap<>();

        memberService.onUnFollow(ffrom, fto);
        result.put("message", "OK");

        return result;
    }

    @GetMapping("/getFollowings")
    public List<Follow> getFollowings(@RequestParam("nickname") String nickname){

        List<Follow> list = memberService.getFollowings(nickname);
        return list;
    }

    @GetMapping("/getFollowers")
    public List<Follow> getFollowers(@RequestParam("nickname") String nickname){

        List<Follow> list = postService.getFollowers(nickname);
        return list;

    }

    @PostMapping("/updateProfile")
    public HashMap<String, Object> updateProfile(@RequestBody Member member, HttpServletRequest request){
        HashMap<String, Object> result = new HashMap<>();

        postService.updateProfile(member);
//        HttpSession session = request.getSession();
//        session.setAttribute("loginUser", member);

        PasswordEncoder passwordEncoder = customSecurityConfig.passwordEncoder();
        member.setPwd(passwordEncoder.encode(member.getPwd()));
        result.put("loginUser", member);
        result.put("message", "OK");
        return result;
    }

    @GetMapping("/getMemberInfo")
    public HashMap<String, Object> getMemberInfo(@RequestParam("nickname") String membernick){
        HashMap<String, Object> result = new HashMap<>();

        Member member = memberService.getFindNickName(membernick);

        List<Follow> followingList = memberService.getFollowings(membernick);
        List<Follow> followersList = memberService.getFollowers(membernick);

        result.put("cuser", member);
        result.put("followers", followersList);
        result.put("followings", followingList);

        return result;
    }

    @GetMapping("/refresh/{refreshToken}")
    public Map<String, Object> refresh(@RequestHeader("Authorization") String authHeader, @PathVariable("refreshToken") String refreshToken) throws CustomJWTException {

        if(refreshToken == null) throw new CustomJWTException("NULL_REFRESH");
        if(authHeader == null || authHeader.length() < 7){
            throw new CustomJWTException("INVALID_HEADER");
        }

        // 추출한 내용의 7번째 글자부터 끝까지 추출
        String accessToken = authHeader.substring(7); // 기간이 지나면 true, 안지났으면 false 를 리턴

        if(checkExpiredToken(accessToken)){ // 기간이 지나면 true, 안지났으면 false 를 리턴
            return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
        }

        //accessToken 기간 만료시 refreshToken 으로 재검증하여 사용자 정보 추출
        Map<String, Object> claims = JWTUtil.validateToken(refreshToken);

        //토큰 교체
        String newAccessToken = JWTUtil.generateToken(claims, 5);

        String newRefreshToken = "";
        if(checkTime((Integer)claims.get("exp"))){
            newRefreshToken = JWTUtil.generateToken(claims, 60*24);
        }else {
            newRefreshToken = refreshToken;
        }

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    private boolean checkTime(Integer exp) {
        java.util.Date expDate = new java.util.Date((long)exp * (1000)); // 날짜로 변환
        long gap = expDate.getTime() - System.currentTimeMillis(); //현재 시간과의 차이 계산
        long leftMin = gap / (1000 * 60); // 분단위 계산
        // 1시간도 안남았는지..
        return leftMin < 60;
    }

    private boolean checkExpiredToken(String accessToken) {
        try{
            JWTUtil.validateToken(accessToken);
        }catch(CustomJWTException ex){
            if(ex.getMessage().equals("Expired")){
                return true;
            }
        }
        return false;
    }
}
