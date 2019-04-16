package com.imitatezhihu.rocketmq;

import com.imitatezhihu.async.EventModel;
import com.imitatezhihu.async.EventType;
import com.imitatezhihu.model.EntityType;
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
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FollowConsumer extends Consumer implements CommandLineRunner {
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

            try {
                DefaultMQPushConsumer consumer = consumerSetting(consumerGroup, namesrvAddr,"topic",tags,ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET
                ,MessageModel.BROADCASTING, 32);
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

