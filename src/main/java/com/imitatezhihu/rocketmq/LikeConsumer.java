package com.imitatezhihu.rocketmq;

import com.alibaba.fastjson.JSON;
import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.MessageService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.WendaUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class LikeConsumer implements Consumer,CommandLineRunner {
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
        //消费者的组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        //consumer.setConsumeThreadMin(5);
        consumer.setInstanceName(String.valueOf(UUID.randomUUID()));
        consumer.setNamesrvAddr(namesrvAddr);
        try {

            consumer.subscribe("topic",tags);

            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setConsumeMessageBatchMaxSize(32);

            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

                for(Message msg:msgs){
                    String str = new String(msg.getBody());
                    EventModel eventModel = JSON.parseObject(str,EventModel.class);
                    doHandle(eventModel);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
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
