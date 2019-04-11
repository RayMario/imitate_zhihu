package com.imitatezhihu.controller;

import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventProducer;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.HostHolder;
import com.imitatezhihu.model.Question;
import com.imitatezhihu.rocketmq.Producer;
import com.imitatezhihu.service.FollowService;
import com.imitatezhihu.service.QuestionService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;

@Controller
public class Test {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    FollowService followService;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    QuestionService questionService;
    @Autowired
    Producer producer;
//    @Autowired
//    RocketMQTemplate rocketMQTemplate;

    @RequestMapping(path = {"/test1"},method = {RequestMethod.GET})
    @ResponseBody
    public String testMq(){
    //此页面用来测试消息队列
        Question q = questionService.selectById(1);
        boolean ret = followService.follow(1, EntityType.ENTITY_QUESTION, 1);

//        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
//                .setActorId(1).setEntityId(1)
//                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));
        try {
            producer.send(new EventModel(EventType.FOLLOW)
                    .setActorId(1).setEntityId(1)
                    .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));
        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        boolean ret1 = followService.unfollow(1, EntityType.ENTITY_QUESTION, 1);
        return "end";
    }
}
