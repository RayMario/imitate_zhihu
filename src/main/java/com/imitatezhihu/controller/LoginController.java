package com.imitatezhihu.controller;

import com.imitatezhihu.async.EventProducer;
import com.imitatezhihu.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*注册：当前user入库，同时生成ticket入库，通过cookie下发ticket
*登录：验证用户名与密码，验证ticket是否存在且正确——ticket的验证交给拦截器。之后返回next实现重定向
* 重登录：将当前页面的url记录下来，同时返回到登录页面
* 登出：将当前ticket的状态设置为无效，下次登录会被拦截器检查出来，即可实现登出。
 */
@Controller
public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    //注册页面，注册完了也要给一个ticket
    @RequestMapping(path = {"/reg"}, method = {RequestMethod.POST})
    public String reg(Model model,
                      @RequestParam( name = "username",required = false) String username,
                      @RequestParam( name = "password",required = false) String password,
                      @RequestParam(value = "next",required = false) String next,
                      HttpServletResponse response) {
        try {

            Map<String, String> map = userService.register(username, password);
            //处理Service传过来的map信息
            if(map.containsKey("ticket")){
                //如果存在ticket，则通过cookie下发
                Cookie cookie = new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);
                if(!StringUtils.isEmpty(next)){
                    return "redirect:" +next;
                }
                return "redirect:/";
            }else{//如果不包含ticket，那么重新返回登录页面，并显示错误信息。
                model.addAttribute("msg",map.get("msg"));
                return "login";
            }
        }catch (Exception e){
            logger.error("注册异常"+ e.getMessage());
            return "login";
        }
    }

    //用于记录当前访问信息的登录。next记录当前访问页面。其中next的来源是拦截器从url当中截取出来的
    @RequestMapping(path = {"/reglogin"},method = {RequestMethod.GET})
    public String reg(Model model,
                      @RequestParam(value = "next",required = false) String next){
        model.addAttribute("next",next);
        return "login";
    }

    //登录页面
    @RequestMapping(path = {"/login/"},method = {RequestMethod.POST})
    public String login(Model model,
                        @RequestParam(name = "username") String username,
                        @RequestParam(name = "password") String password,
                        @RequestParam(value = "next") String next,
                        @RequestParam(value = "remeberme",defaultValue = "false") boolean rememberme,
                        HttpServletResponse response){
        try{

            Map<String,Object> map = userService.login(username,password);
            /*如果用户已经有ticket，那么生成一个cookie，将Service层map中放入的ticket放进cookie当中
             并将cookie放入response当中，其中response是HttpServletResponse类的实例*/
            if(map.containsKey("ticket")){
                Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
                cookie.setPath("/");
                response.addCookie(cookie);

                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);

                //登录事件进队列，此处留给以后异常ip检测
//                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
//                        .setExts("username",username).setExts("email","2061283204@qq.com")
//                        .setActorId((int)map.get("userId")));

                if(!StringUtils.isEmpty(next)){
                    return "redirect:" +next;
                }
                return "redirect:/";
            }else{//如果不包含ticket，那么重新返回登录页面，并显示错误信息。
                model.addAttribute("msg",map.get("msg"));
                return "login";
            }
        }catch (Exception e){
            logger.error("登录异常"+e.getMessage());
            return "login";
        }

    }
    //退出页面
    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/";
    }
}
