package com.imitatezhihu.rocketmq;

import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.MessageService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.WendaUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LikeConsumer extends Consumer implements CommandLineRunner {
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;

    @Value("${apache.rocketmq.consumer.PushConsumer2}")
    protected String consumerGroup;
    @Value("${apache.rocketmq.namesrvAddr}")
    public String namesrvAddr;

    String tags;

    @Override
    public void messageListener() {
        tags = String.valueOf(EventType.LIKE.getValue());

        try {
            DefaultMQPushConsumer consumer = consumerSetting(consumerGroup, namesrvAddr,"topic",tags,ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET
                    ,MessageModel.BROADCASTING, 32);
//            consumer.subscribe("topic",tags);
//
//            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
//            consumer.setConsumeMessageBatchMaxSize(32);
//
//            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
//
//                for(Message msg:msgs){
//                    String str = new String(msg.getBody());
//                    EventModel eventModel = JSON.parseObject(str,EventModel.class);
//                    doHandle(eventModel);
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            });
            consumer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doHandle(EventModel eventModel) {
        com.imitatezhihu.model.Message message = new com.imitatezhihu.model.Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(eventModel.getEntityOwnerId());
        User user = userService.getUser(eventModel.getActorId());
        message.setContent("用户"+user.getName()+"赞了你的评论,http://127.0.0.1:8080/question/"+eventModel.getExts("questionId"));
        message.setCreatedDate(new Date());
        System.out.println("赞同信息已处理");
        messageService.addMessage(message);
    }

    @Override
    public void run(String... args) throws Exception {
        this.messageListener();
    }
}
