package com.imitatezhihu.controller;

import com.alibaba.fastjson.JSON;
import com.imitatezhihu.model.*;
import com.imitatezhihu.service.CommentService;
import com.imitatezhihu.service.FollowService;
import com.imitatezhihu.service.QuestionService;
import com.imitatezhihu.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//前端页面展示，用户详情页面展示
@Controller
public class IndexController {
    @Autowired
    QuestionService questionService;
    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;
    @Autowired
    FollowService followService;
    @Autowired
    HostHolder hostHolder;


    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    /*问题信息&& 关注信息&& 用户信息 整合成一个list，方便前端循环
    ——不建议使用 自定义类 再次封装，可能导致后续fastJSON无法正确转换成JSON串
     */
    private List<Map<String,Object>> getQuestions(int userId,int offset, int limit){
        List<Question> questionList = questionService.getLatestQuestion(userId,offset,limit);
        List<Map<String,Object>> vos = new ArrayList<>();
        for (Question question:questionList){
            Map<String,Object> vo = new HashMap<String, Object>();
            vo.put("question",question);
            vo.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.put("user",userService.getUser(question.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    //初始首页展示，以页面形式返回
    @RequestMapping(path = {"/","/index"},method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model){

        model.addAttribute("vos", getQuestions(0, 0, 10));

        return "index";
    }

    //ajax响应前端的下拉刷新，此时数据以JSON格式返回，实现动态更新
    @RequestMapping(path = {"/ajax"},method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String indexAjax(Model model, Integer offset){
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> map = new HashMap<String, Object>();
        if(offset == null) {
            result  = getQuestions(0, 0, 10);
        }else {
            result  = getQuestions(0, offset, 10);
        }
        model.addAttribute("results", result);
        String jstring = JSON.toJSONString(result);
        return jstring;
    }

    //展示用户详情页面
    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos", getQuestions(userId, 0, 10));

        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }


}
