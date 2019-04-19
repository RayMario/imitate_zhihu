package com.imitatezhihu.controller;

import com.imitatezhihu.Interceptor.SameUrlData;
import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventProducer;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.Comment;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.HostHolder;
import com.imitatezhihu.rocketmq.Producer;
import com.imitatezhihu.service.CommentService;
import com.imitatezhihu.service.LikeService;
import com.imitatezhihu.util.WendaUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;

@Controller
public class LikeController {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    LikeService likeService;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    CommentService commentService;
    @Autowired
    Producer producer;
    //点赞操作
    @RequestMapping(path = {"/like"},method = {RequestMethod.POST})
    @ResponseBody
    @SameUrlData
    public String like(@RequestParam("commentId") int commentId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }
        //将点赞信息提交给异步消息队列
        Comment comment = commentService.getCommentById(commentId);
//        eventProducer.fireEvent(new EventModel(EventType.LIKE)
//                .setActorId(hostHolder.getUser().getId()).setEntityId(commentId).setEntityOwnerId(comment.getUserId())
//                .setEntityType(EntityType.ENTITY_COMMENT).setExts("questionId",String.valueOf(comment.getEntityId())));
        if(likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,commentId) <= 0) {
            try {
                producer.send(new EventModel(EventType.LIKE)
                        .setActorId(hostHolder.getUser().getId()).setEntityId(commentId).setEntityOwnerId(comment.getUserId())
                        .setEntityType(EntityType.ENTITY_COMMENT).setExts("questionId", String.valueOf(comment.getEntityId())));
            } catch (InterruptedException e) {
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
            //读取redis赞set，返回当前点赞数量
            long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
            return WendaUtil.getJSONString(0, String.valueOf(likeCount));
        }else{
            long likeCount = likeService.removeLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);

            return WendaUtil.getJSONString(0, String.valueOf(likeCount));
        }

    }

    //点踩操作
    @RequestMapping(path = {"/dislike"},method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }
        //读取redis的赞set，返回更新后赞的数量
        long likeCount = likeService.disLike(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,commentId);
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));
    }
}
