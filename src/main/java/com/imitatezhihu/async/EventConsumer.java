package com.imitatezhihu.async;

import com.alibaba.fastjson.JSON;
import com.imitatezhihu.util.JedisAdapter;
import com.imitatezhihu.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

//事件消费者——多线程从事件队列当中pop事件，事件分发。
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {

    @Autowired
    JedisAdapter jedisAdapter;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();
    private ApplicationContext applicationContext;
    private static ArrayBlockingQueue<String> eventCache = new ArrayBlockingQueue<String>(100);


    @Override
    public void afterPropertiesSet() throws Exception {
        //按类型获取bean，返回值{bean名（类名）= 类，。。。}
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans!= null){
            //这两个循环的目的是从一张动态的handler-event表统计出一张event-handler表。
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()){
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for (EventType type : eventTypes){
                    if(!config.containsKey(type)){
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    //更新type支持的Handler类表
                    config.get(type).add(entry.getValue());
                }
            }
        }
        //批量读取redis当中的消息进jvm内存
        Thread threadCache = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    cacheMq();
                }
            }
        });


        //队列取出并寻找Handler
        Thread threadConsume = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){

                    String key = RedisKeyUtil.getEventQueueKey();
                    /*利用线程取redis队列当中的消息时，消息消费的速度是比较慢的，
                    会导致redis当中堆积一个很大的list，高峰时期会占满redis内存。
                    改为利用jvm内存帮助redis缓存。
                     */

                   try {
                        String message = eventCache.take();

                        EventModel eventModel = JSON.parseObject(message,EventModel.class);
                        //在event-handler表中查询
                        if(!config.containsKey(eventModel.getType())){
                            logger.error("不能处理本类事件");
                            continue;
                        }
                        //观察redis list长度
                       //System.out.println(jedisAdapter.llen(key));
                        //该type所有该经过的Handler
                        for(EventHandler handler:config.get(eventModel.getType())){
                            handler.doHandler(eventModel);
                        }
                    } catch (InterruptedException e) {
                    e.printStackTrace();
                    }
                }
            }
        });
        threadConsume.start();
        threadCache.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public void cacheMq(){
        String key = RedisKeyUtil.getEventQueueKey();
        while (eventCache.size()<=100 && jedisAdapter.llen(key)>0){
            List<String> events = jedisAdapter.brpop(0, key);
            for (String message : events) {
                if (message.equals(key)) {
                    continue;
                }
                boolean res =  eventCache.offer(message);
            }
        }
    }
}
