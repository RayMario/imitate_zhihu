package com.imitatezhihu.async;

import com.alibaba.fastjson.JSONObject;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.Feed;
import com.imitatezhihu.model.Question;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.FeedService;
import com.imitatezhihu.service.FollowService;
import com.imitatezhihu.service.QuestionService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.JedisAdapter;
import com.imitatezhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
//Timeline的推模式Handler
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    UserService userService;
    @Autowired
    QuestionService questionService;
    @Autowired
    FeedService feedService;
    @Autowired
    FollowService followService;
    @Autowired
    JedisAdapter jedisAdapter;

    //将每个推送信息打包
    private String buildFeedData(EventModel eventModel){
        Map<String,String> map = new HashMap<String,String>();
        //记录当前登录用户
        User actor = userService.getUser(eventModel.getActorId());
        if(actor == null){
            return null;
        }
        map.put("userId",String.valueOf(actor.getId()));
        map.put("userHead",actor.getHeadUrl());
        map.put("userName",actor.getName());
        //评论或关注一个问题，则将这个问题的title也加入到map当中去
        if(eventModel.getType() == EventType.COMMENT ||
                eventModel.getType() == EventType.FOLLOW && eventModel.getEntityType() == EntityType.ENTITY_QUESTION){
            Question question = questionService.selectById(eventModel.getEntityId());
            if(question == null){
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    //timeline的推模式
    @Override
    public void doHandler(EventModel eventModel) {

        Feed feed = new Feed();
        feed.setUserId(eventModel.getActorId());
        //注意要传进EVENT_TYPE而不是ENTITY_TYPE
        feed.setType(eventModel.getType().getValue());
        feed.setCreatedDate(new Date());
        feed.setData(buildFeedData(eventModel));
        if(feed.getData() == null){
            //防止出现NPE
            return;
        }
        feedService.addFeed(feed);

        //获得所有粉丝
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER,eventModel.getActorId(),Integer.MAX_VALUE);
        //系统队列
        followers.add(0);
        //使用redis在每个人的timeLine当中添加当前的Feed
        for(int follower:followers){
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            //由于feed本身可能比较大，因此redis当中只记录feed的Id
            jedisAdapter.lpush(timelineKey,String.valueOf(feed.getId()));
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.COMMENT, EventType.FOLLOW);
    }
}
