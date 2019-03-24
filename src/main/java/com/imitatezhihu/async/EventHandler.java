package com.imitatezhihu.async;


import java.util.List;
//Handler父接口
public interface EventHandler {
    void doHandler(EventModel eventModel);

    List<EventType> getSupportEventTypes();
}
