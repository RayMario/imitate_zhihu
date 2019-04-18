package com.imitatezhihu.rocketmq;

import com.alibaba.fastjson.JSON;
import com.imitatezhihu.async.EventModel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.UUID;

public abstract class Consumer {
     abstract void messageListener();
     abstract void doHandle(EventModel eventModel);
     protected DefaultMQPushConsumer consumerSetting(String consumerGroup, String namesrvAddr, String topic
             ,String tags,ConsumeFromWhere consumeFromWhere, MessageModel messageModel, int batchSize){

          DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
          consumer.setInstanceName(String.valueOf(UUID.randomUUID()));
          consumer.setNamesrvAddr(namesrvAddr);
          try {
               consumer.subscribe(topic,tags);
          } catch (MQClientException e) {
               e.printStackTrace();
          }
          //consumer.setConsumeThreadMin(5);
          consumer.setConsumeFromWhere(consumeFromWhere);
          //分发模式
          consumer.setMessageModel(messageModel);
          //控制每次读取的消息数目：底层是一个ArrayList做的缓存
          consumer.setConsumeMessageBatchMaxSize(batchSize);
          //在此监听中消费信息，并返回消费的状态信息
          consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

               for(Message msg:msgs){

                    String str = new String(msg.getBody());
                    EventModel eventModel = JSON.parseObject(str,EventModel.class);
                    doHandle(eventModel);
               }

               return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
          });
          return consumer;
     }
}

