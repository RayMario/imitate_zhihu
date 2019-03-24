package com.imitatezhihu.controller;

import com.imitatezhihu.model.HostHolder;
import com.imitatezhihu.model.Message;
import com.imitatezhihu.model.User;
import com.imitatezhihu.model.ViewObject;
import com.imitatezhihu.service.MessageService;
import com.imitatezhihu.service.UserService;
import com.imitatezhihu.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
* 站内信发送：站内信入库即可。因为前端还没有实现实时弹窗实时提醒，因此无需进入消息队列
* 显示与所有人交互时收到的最后一条消息，返回一个页面
*显示与某人交互细节，返回一个页面
* */
@Controller
public class MessageController {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(MessageController.class);

    //站内信入库，同时通过JSON返回站内信发送状态
    @RequestMapping(path = {"/msg/addMessage"},method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content){
        try{
            if(hostHolder.getUser() == null){
                return WendaUtil.getJSONString(999,"未登录");
            }
            //这个user是对手方user
            User user = userService.selectByName(toName);
            if(user == null){
                return WendaUtil.getJSONString(1,"用户不存在");
            }

            Message message = new Message();
            message.setCreatedDate(new Date());
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setContent(content);
            messageService.addMessage(message);
            return WendaUtil.getJSONString(0);
        }catch(Exception e){
            logger.error("发送消息失败"+e.getMessage());
            return WendaUtil.getJSONString(1,"发信失败");
        }
    }

    //显示与所有人交互时收到的最后一条消息
    @RequestMapping(path = {"/msg/list"},method = {RequestMethod.GET})
    public String getConversationList(Model model){
        if(hostHolder.getUser() == null){
            return "redirect:/reglogin";
        }
        //我的userId
        int localUserId = hostHolder.getUser().getId();
        List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
        List<ViewObject> conversations = new ArrayList<ViewObject>();
        for(Message message : conversationList){
            ViewObject vo = new ViewObject();
            vo.set("message",message);
            //message都是一条message，但前端肯定希望显示的是对方的id，而不是自己的id。这样可以显示对方的头像等
            int targetId = message.getFromId() == localUserId ? message.getToId():message.getFromId();
            vo.set("user",userService.getUser(targetId));
            vo.set("unread",messageService.getConversationUnreadCount(localUserId,message.getConversationId()));
            conversations.add(vo);
        }
        model.addAttribute("conversations", conversations);
        return "letter";
    }

    //显示与某人的所有消息细节
    @RequestMapping(path = {"/msg/detail"},method = {RequestMethod.GET})
    public String getConversationDetail(Model model,@RequestParam("conversationId") String conversationId){
        if(hostHolder.getUser() == null){
            return "redirect:/reglogin";
        }
        try {
            List<Message> messageList = messageService.getConversationDetail(conversationId,0,10);
            List<ViewObject> messages = new ArrayList<ViewObject>();
            int localUserId = hostHolder.getUser().getId();
            int fromId = messageService.getPosterId(localUserId,conversationId);
            for(Message message:messageList){
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                vo.set("user",userService.getUser(message.getFromId()));
                messages.add(vo);
            }
            model.addAttribute("messages",messages);
            if(messageList !=null){
                messageService.updateHasRead(fromId,localUserId);
            }
        }catch (Exception e){
            logger.error("获取详情失败" +e.getMessage());
        }
        return "letterDetail";
    }
}
