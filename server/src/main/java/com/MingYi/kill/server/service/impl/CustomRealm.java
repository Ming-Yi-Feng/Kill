package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.model.entity.User;
import com.MingYi.kill.model.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * 用于shiro的认证授权
 */
public class CustomRealm extends AuthorizingRealm {

    @Autowired
    private UserMapper userMapper;
    /**
     * 授权

     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 认证 -- 登录
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String useName = token.getUsername();
        String passWord = String.valueOf(token.getPassword());
        User user = userMapper.selectByUserName(useName);


        if(user == null){
            throw new UnknownAccountException("用户不存在");
        }
        if(!Objects.equals(1,user.getIsActive().intValue())) {
            throw new DisabledAccountException("用户被禁用");
        }
        if(!user.getPassword().equals(passWord)){
            throw new IncorrectCredentialsException("用户名密码不匹配");
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user.getId(),passWord,"CustomRealm");
        setSession("uid",user.getId());
        return info;
    }

    //TODO:登录成功后，存入Shiro的session -- 最终给HttpSession进行管理。如果是分布式session，则给Redis
    private void setSession(String key,Object value){
        Session session = SecurityUtils.getSubject().getSession();
        if(session != null){
            session.setAttribute(key,value);
            session.setTimeout(30_000L);
        }
    }
}
