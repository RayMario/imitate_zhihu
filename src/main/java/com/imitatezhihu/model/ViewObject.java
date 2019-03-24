package com.imitatezhihu.model;

import java.util.HashMap;
import java.util.Map;

//注意本类当中没有完全重载Map当中的各种方法，如果需要用转化成JSON，请不要用此类进行封装。
//适合直接用model进行最后一次封装的静态页面使用。
public class ViewObject {
    private Map<String,Object> objs = new HashMap<String,Object>();

    public void set(String key,Object value){
        objs.put(key,value);
    }
    public Object get(String key){
        return objs.get(key);
    }
}
