package com.imitatezhihu.configuration;

import com.imitatezhihu.Interceptor.LoginRequiredInterceptor;
import com.imitatezhihu.Interceptor.PassportInterceptor;
import com.imitatezhihu.Interceptor.SameUrlDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//拦截器注册
@Component
public class WendaWebConfiguration implements WebMvcConfigurer {
    @Autowired
    PassportInterceptor passportInterceptor;
    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    SameUrlDataInterceptor sameUrlDataInterceptor;
    //重写添加拦截器方法，使得刚刚定义的拦截器module可以加载到Ioc容器当中去。
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor);
        //其中重定向的拦截器只拦截与user有关的页面：不登录还是可以浏览问题信息的
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("*/user/*");

        registry.addInterceptor(sameUrlDataInterceptor);
    }


}
