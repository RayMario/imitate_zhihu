package com.imitatezhihu.controller;

import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventProducer;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.Comment;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.HostHolder;
import com.imitatezhihu.rocketmq.Producer;
import com.imitatezhihu.service.CommentService;
import com.imitatezhihu.service.QuestionService;
import com.imitatezhihu.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
//添加评论：评论入库+更新评论数量+评论事件加入消息队列
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;
    @Autowired
    QuestionService questionService;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    Producer producer;

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            //评论入库
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
            }
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            //更新评论数量：
            int count = commentService.getCommentCount(comment.getEntityId(),comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(),count);

            //评论进入事件队列
//            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(hostHolder.getUser().getId())
//            .setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId)
//                    .setEntityOwnerId(questionService.selectById(questionId).getUserId()));

            producer.send(new EventModel(EventType.COMMENT).setActorId(hostHolder.getUser().getId())
                    .setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId)
                    .setEntityOwnerId(questionService.selectById(questionId).getUserId()));

        }catch (Exception e){
            logger.error("增加评论失败" +e.getMessage());
        }
        return "redirect:/question/" +questionId;
    }
}
