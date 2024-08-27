package com.himedia.spserver.controller;

import com.himedia.spserver.entity.*;
import com.himedia.spserver.service.PostService;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/getPostList") // @RequestParam 에 value="word", required = false 을 넣으면 해시태그 검색에서 아무것도 안온 것을 대비하기 위한 것. 이렇게 하면 아무것도 입력을 안해도 오류가 발생하지 않음.
    public HashMap<String, Object> getPostList(@RequestParam(value="word", required = false) String word){
        HashMap<String, Object> result = new HashMap<>();

        result.put("postList", postService.getPostList(word));

        return result;
    }

    @GetMapping("/getImages/{postid}")
    public List<Images> getImages(@PathVariable("postid") int postid){

        List<Images> list = postService.getImages(postid);


        return list;
    }

    @GetMapping("/getLikes/{postid}")
    public List<Likes> getLikes(@PathVariable("postid") int postid){
        List<Likes> list = postService.getLikes(postid);


        return list;
    }

    @GetMapping("/getReplyList/{postid}")
    public List<Reply> getReplyList(@PathVariable("postid") int postid){
        List<Reply> list = postService.getReplyList(postid);

        return list;
    }
    @PostMapping("/addlike")
    public HashMap<String, Object> addlike(@RequestParam("postid") int postid, @RequestParam("likenick") String likenick){
        HashMap<String, Object> result = new HashMap<>();

        Likes likes = postService.addLike(postid, likenick);

        if(likes == null){ // 없으면
            postService.addInsertLike(postid, likenick);
        }
        else{ // 있으면
            postService.deleteLike(postid, likenick);
        }

        return null;
    }

    @PostMapping("/addReply/{postid}")
    public HashMap<String, Object> addReply(@RequestBody Reply reply, @PathVariable("postid") int postid){
        HashMap<String, Object> result = new HashMap<>();
        postService.addReply(reply, postid);
        return null;
    }

    @DeleteMapping("/deleteReply/{id}")
    public HashMap<String, Object> deleteReply(@PathVariable("id") int id){
        postService.deleteReply(id);
        return null;
    }

    @Autowired
    ServletContext servletContext;

    @PostMapping("/imgup")
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
            result.put("savefilename", fn1 + dt + fn2);
        }catch(IllegalStateException | IOException e){
            e.printStackTrace();
        }

        return result;
    }

    @PostMapping("/insertPost")
    public HashMap<String, Object> insertPost(@RequestBody Post post){
        HashMap<String, Object> result = new HashMap<>();

        Post post1 = postService.insertPost(post);

        result.put("id", post1.getId());

        return result;
    }

    @PostMapping("/insertImages")
    public HashMap<String, Object> insertImages(@RequestBody Images images){
        HashMap<String, Object> result = new HashMap<>();
        postService.insertImages(images);
        return result;
    }

    @GetMapping("/getMyPost")
    public HashMap<String, Object> getMyPost(@RequestParam("nickname") String nickname){
        HashMap<String, Object> result = new HashMap<>();
        List<Post> list = postService.getPostListByNickname(nickname);
        List<String> imgList = new ArrayList<>();
        for(Post p : list){
            List<Images> imgl = postService.getImgListByPostid(p.getId());
            String imgName = imgl.get(0).getSavefilename();
            imgList.add(imgName);
        }
        result.put("postList", list);
        result.put("imgList", imgList);
        return result;
    }

    @GetMapping("/getPost/{postid}")
    public Post getPost(@PathVariable("postid") int postid){
        Post post = postService.getPost(postid);
        return post;
    }

//    @GetMapping("/getFollowings")
//    public List<Follow> getFollowings(@RequestParam("nickname") String nickname){
//
//        List<Follow> list = postService.getFollowings(nickname);
//        return list;
//
//    }
}
