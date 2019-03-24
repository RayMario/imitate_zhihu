package com.imitatezhihu.async;

import java.util.HashMap;
import java.util.Map;
//事件模型类
public class EventModel {
    private EventType type;
    //事件发出者的Id
    private int actorId;
    //下面这俩代表事件的对手方
    private int entityType;
    private int entityId;
    //事件对手方的拥有者
    private int entityOwnerId;
    private Map<String, String> exts = new HashMap<String, String>();

    public EventModel(EventType eventType){
        this.type = eventType;
    }
    //默认构造函数
    public EventModel(){

    }

    public EventModel setExts(String key, String value){
        exts.put(key,value);
        return this;
    }
    public String getExts(String key){
        return exts.get(key);
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel setExts(Map<String, String> exts) {
        this.exts = exts;
        return this;
    }
}
