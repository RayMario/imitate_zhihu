package com.imitatezhihu.model;

import org.springframework.stereotype.Component;

//这个类的对象专门放拦截器刚取出来的对象，供给其他地方使用
@Component
public class HostHolder {
    //又因为一定会有多个用户同时访问，因此一定是用线程来写：每个线程的内存都不一样,
    // 根据线程自己找自己的地址，同时对外的接口名还是相同的
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser(){
        return users.get();
    }
    public void setUser(User user){
        users.set(user);
    }
    public void clear(){
        users.remove();
    }
}
