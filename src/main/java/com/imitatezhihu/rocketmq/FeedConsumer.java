package com.imitatezhihu.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.Feed;
import com.imitatezhihu.model.Question;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.*;
import com.imitatezhihu.util.JedisAdapter;
import com.imitatezhihu.util.RedisKeyUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedConsumer extends Consumer implements CommandLineRunner {
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;
    @Autowired
    FeedService feedService;
    @Autowired
    FollowService followService;
    @Autowired
    JedisAdapter jedisAdapter;
    @Autowired
    QuestionService questionService;

    @Value("${apache.rocketmq.consumer.PushConsumer1}")
    protected String consumerGroup;
    @Value("${apache.rocketmq.namesrvAddr}")
    public String namesrvAddr;

    String tags;

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


    @Override
    public void messageListener() {
        tags = String.valueOf(EventType.FOLLOW.getValue())+"||"
        +String.valueOf(EventType.COMMENT.getValue());

        try {
            DefaultMQPushConsumer consumer = consumerSetting(consumerGroup, namesrvAddr,"topic",tags,ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET
                    ,MessageModel.BROADCASTING, 32);
            consumer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void doHandle(EventModel eventModel) {
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
    public void run(String... args) throws Exception {
        this.messageListener();
    }
}
