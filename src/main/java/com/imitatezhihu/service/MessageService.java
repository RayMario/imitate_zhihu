package com.imitatezhihu.service;

import com.imitatezhihu.dao.MessageDao;
import com.imitatezhihu.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    MessageDao messageDao;

    @Autowired
    SensitiveService sensitiveService;

    //站内信先经过敏感词过滤，再入库
    public int addMessage(Message message){
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDao.addMessage(message);
    }

    //从数据库读取与某人的详细通话记录
    public List<Message> getConversationDetail(String conversationId, int offset, int limit){
        return  messageDao.getConversationDetail(conversationId,offset,limit);
    }

    //从数据库获取与所有人的最后一条通信记录
    public List<Message> getConversationList(int userId, int offset, int limit){
        return messageDao.getConversationList(userId, offset, limit);
    }

    //未读信息显示
    public int getConversationUnreadCount(int userId, String conversationId){
        return messageDao.getConversationUnreadCount(userId,conversationId);
    }
    //未读信息标记清除
    public void updateHasRead(int fromId, int userId){
        messageDao.updateHasRead(fromId,userId,1);
    }
    //给一条conversationId信息，以及本地用户id，返回其发件人
    public int getPosterId(int userId, String conversationId){
        return messageDao.getPosterId(userId, conversationId);
    }
}
