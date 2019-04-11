package com.imitatezhihu.rocketmq;

import com.alibaba.fastjson.JSON;
import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.EntityType;
import com.imitatezhihu.model.User;
import com.imitatezhihu.service.MessageService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.WendaUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FollowConsumer implements Consumer,CommandLineRunner {
    /**
     * 消费者的组名
     */
    @Value("${apache.rocketmq.consumer.PushConsumer}")
    protected String consumerGroup;
    /**
     * NameServer地址
     */
    @Value("${apache.rocketmq.namesrvAddr}")
    public String namesrvAddr;

    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;

    //mq并发测试用编号i
    AtomicInteger i = new AtomicInteger(0);
    String tags;

    @Override
    public void messageListener() {
            tags = String.valueOf(EventType.FOLLOW.getValue());
            //消费者的组名
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setInstanceName(String.valueOf(UUID.randomUUID()));
            //指定NameServer地址，多个地址以 ; 隔开
            consumer.setNamesrvAddr(namesrvAddr);
            try {

                consumer.subscribe("topic",tags);
                //consumer.setConsumeThreadMin(5);
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                //分发模式
                consumer.setMessageModel(MessageModel.BROADCASTING);
                //控制每次读取的消息数目：底层是一个ArrayList做的缓存
                consumer.setConsumeMessageBatchMaxSize(32);
                //在此监听中消费信息，并返回消费的状态信息
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
        };

    @Override
    public void doHandle(EventModel eventModel) {

        com.imitatezhihu.model.Message message = new com.imitatezhihu.model.Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(eventModel.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(eventModel.getActorId());

        if (eventModel.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + user.getName()
                    + "关注了你的问题,http://127.0.0.1:8080/question/" + eventModel.getEntityId());
        } else if (eventModel.getEntityType() == EntityType.ENTITY_USER) {
            message.setContent("用户" + user.getName()
                    + "关注了你,http://127.0.0.1:8080/user/" + eventModel.getActorId());
        }
        //mq的并发测试用
        System.out.println("关注信息已处理"+Integer.toString(i.getAndIncrement()));
    }
    @Override
    public void run(String... args) throws Exception {
        this.messageListener();
    }
}

