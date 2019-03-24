package com.imitatezhihu.async;

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

//点赞Handler——触发站内信
@Component
public class likeHandler implements EventHandler {
    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;
    @Override
    public void doHandler(EventModel eventModel) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(eventModel.getEntityOwnerId());
        User user = userService.getUser(eventModel.getActorId());
        message.setContent("用户"+user.getName()+"赞了你的评论,http://127.0.0.1:8080/question/"+eventModel.getExts("questionId"));
        message.setCreatedDate(new Date());

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
