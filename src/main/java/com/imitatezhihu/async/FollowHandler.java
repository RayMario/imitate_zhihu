package com.imitatezhihu.async;

import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.Message;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.MessageService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

//关注用户与关注问题Handler——触发站内信
@Component
public class FollowHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    //mq并发测试用编号i
//    int i = 0;

    @Override
    public void doHandler(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());

        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + user.getName()
                    + "关注了你的问题,http://127.0.0.1:8080/question/" + model.getEntityId());
        } else if (model.getEntityType() == EntityType.ENTITY_USER) {
            message.setContent("用户" + user.getName()
                    + "关注了你,http://127.0.0.1:8080/user/" + model.getActorId());
        }
        //mq的并发测试用
//        System.out.println("关注信息已处理"+Integer.toString(i++));

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW);
    }

}
