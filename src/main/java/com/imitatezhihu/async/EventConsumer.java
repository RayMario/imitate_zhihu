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
//事件消费者——多线程从事件队列当中pop事件，事件分发。
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {

    @Autowired
    JedisAdapter jedisAdapter;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();
    private ApplicationContext applicationContext;

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

        //队列取出并寻找Handler
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0,key);//timeout = 0意味着一直等待。
                    for(String message:events){
                        //brpop出来的东西是包含key的,应当略过去。
                        if (message.equals(key)){
                            continue;
                        }
                        EventModel eventModel = JSON.parseObject(message,EventModel.class);
                        //在event-handler表中查询
                        if(!config.containsKey(eventModel.getType())){
                            logger.error("不能处理本类事件");
                            continue;
                        }
                        //该type所有该经过的Handler
                        for(EventHandler handler:config.get(eventModel.getType())){
                            handler.doHandler(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
