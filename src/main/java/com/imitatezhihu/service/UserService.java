package com.imitatezhihu.service;

import com.imitatezhihu.dao.LoginTicketDao;
import com.imitatezhihu.dao.UserDao;
import com.imitatezhihu.model.LoginTicket;
import com.imitatezhihu.model.User;
import com.imitatezhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private LoginTicketDao loginTicketDao;
    //注册，通过map返回注册信息
    public Map<String,String> register(String username, String password){
        Map<String,String> map = new HashMap<String,String>();
        if(StringUtils.isEmpty(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if(StringUtils.isEmpty(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        User user = userDao.selectByName(username);
        if(user != null){
            map.put("msg","用户名已被注册");
            return map;
        }
        user =  new User();//正式注册
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));
        user.setPassword(WendaUtil.MD5((password + user.getSalt())));
        userDao.addUser(user);

        //注册成功也需要保存并下发ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);

        return map;
    }

    //通过id查询
    public User getUser(int id){
        return userDao.selectById(id);
    }

    //通过name查询
    public User selectByName(String name){
        return userDao.selectByName(name);
    }

    //登录
    public Map<String,Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isEmpty(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isEmpty(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }
        //当前输入不为空则用Dao层从数据库当中获取个人资料
        User user = userDao.selectByName(username);

        if (user == null) {
            map.put("msg", "用户名不存在");
            return map;
        }
        if(WendaUtil.MD5(password + user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误");
            return map;
        }
        //个人资料更新登录状态。同时tikect需要下发给浏览器
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        map.put("userId", user.getId());
        return map;
    }

    //下发登录状态
    public String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date now = new Date();
        now.setTime(3600*24*100 + now.getTime());
        loginTicket.setExpired(now);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicketDao.addTicket(loginTicket);
        return loginTicket.getTicket();
    }
    //登出
    public void logout(String ticket){
        loginTicketDao.updateStatus(ticket,1);
    }
}
