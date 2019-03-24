package com.imitatezhihu.Interceptor;

import com.imitatezhihu.dao.LoginTicketDao;
import com.imitatezhihu.dao.UserDao;
import com.imitatezhihu.model.HostHolder;
import com.imitatezhihu.model.LoginTicket;
import com.imitatezhihu.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    LoginTicketDao loginTicketDao;
    @Autowired
    UserDao userDao;
    @Autowired
    HostHolder hostHolder;
    //拦截器判断一个用户是否登录，利用ticket信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = null;
        //找到ticket
        if(request.getCookies() != null){
            for (Cookie cookie :request.getCookies()){
                if (cookie.getName().equals("ticket")){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        //验证ticket是否过期
        if(ticket != null){
            LoginTicket loginTicket = loginTicketDao.selectByTicket(ticket);
            if(loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0){
                return true;
            }
            /*现在确定ticket没有过期，那么下面需要将用户信息获取出来,保证controller里面后面的链路都能访问。
            Spring里面要想都能访问，一定是一个依赖注入的形式，这需要新建一个model类：HostHolder*/
            User user = userDao.selectById(loginTicket.getUserId());
            hostHolder.setUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null){
            modelAndView.addObject("user",hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
