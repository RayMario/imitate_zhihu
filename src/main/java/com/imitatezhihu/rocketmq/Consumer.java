package com.imitatezhihu.rocketmq;

import com.imitatezhihu.async.EventModel;

public interface Consumer {
     void messageListener();
     void doHandle(EventModel eventModel);

}

