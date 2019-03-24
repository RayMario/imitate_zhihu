package com.imitatezhihu.controller;

import com.imitatezhihu.model.*;
import com.imitatezhihu.service.*;
import com.imitatezhihu.util.WendaUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
*从前端接收传回来的题目信息；将题目入库；
* 此处没有将question详情直接以JSON形式返回给前端，而是设置了页面刷新，页面刷新会导致从数据库当中重新获得最新题目信息。
* 显示题目详情页面：需要整合question&&Comment&&Follow&&Like信息，打包好传递给前端。
* */
@Controller
public class QuestionController {
    private static final Logger logger = LogManager.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    //hostHolder只用来传递操作者的信息
    @Autowired
    HostHolder hostHolder;

    //userService是用来传递与问题有关的用户的信息
    @Autowired
    UserService userService;

    //commentService
    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    //根据前端收到的title和content，将问题入库
    @RequestMapping(value = "/question/add",method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content){
        try{
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if(hostHolder.getUser() == null){

                question.setUserId(WendaUtil.ANONYMOUS_USERID);
                //如果没登录自动跳到999
                //return WendaUtil.getJSONString(999);
            }else{
                question.setUserId(hostHolder.getUser().getId());
            }
            //提交成功则在json串当中返回0
            if(questionService.addQuestion(question)>0){
                return WendaUtil.getJSONString(0);
            }

        }catch (Exception e){
            logger.error("增加题目失败" +e.getMessage());
        }
        return WendaUtil.getJSONString(1,"失败");
    }

    //题目详情页面，整合question&&Comment&&Follow&&Like信息
    @RequestMapping(value = "question/{qid}")
    public String questionDetail(Model model,
                                 @PathVariable("qid") int qid){
        Question question = questionService.selectById(qid);
        int id = question.getId();
        model.addAttribute("question",question);

        List<Comment> commentList = commentService.getCommentsByEntity(qid,EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for(Comment comment:commentList){
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            Date date = comment.getCreatedDate();
            if(hostHolder.getUser() == null){
                vo.set("liked",0);
            }else{
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("comments",comments);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }
        return "detail";
    }
}
