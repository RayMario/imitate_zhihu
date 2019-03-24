package com.imitatezhihu.async;

import com.imitatezhihu.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//登录异常——触发邮件
@Component
public class LoginExceptionHandler implements EventHandler{

    @Autowired
    MailSender mailSender;

    @Override
    public void doHandler(EventModel eventModel) {
        Map<String, Object> map = new HashMap<String, Object>();
        //这个map是用键值对方式传入参数到前端页面，前端页面现在可以使用${username}了
        map.put("username",eventModel.getExts("username"));
        mailSender.sendWithHTMLTemplate(eventModel.getExts("email"),"登录IP异常",
                "mails/login_exception.html",map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
