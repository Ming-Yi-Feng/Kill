package com.MingYi.kill.server.config;

import com.MingYi.kill.server.service.impl.CustomRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    //TODO: 配置Controller跳转对应的Realm
    @Bean
    public CustomRealm customRealm(){
        return new CustomRealm();
    }

    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(customRealm());
        return defaultWebSecurityManager;
    }

    //TODO:拦截URL。过滤器
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        //这里为什么要注册 Manager??
        shiroFilterFactoryBean.setSecurityManager(securityManager());

        Map<String,String> filter = new HashMap<>();
        shiroFilterFactoryBean.setLoginUrl("/to/login");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unAuth");
        //TODO: 设置拦截规则
        filter.put("/to/login","anon");
        filter.put("/login","anon");
        filter.put("/index","anon");
        filter.put("/*","authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filter);
        return shiroFilterFactoryBean;
    }
}
