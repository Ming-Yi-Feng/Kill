package com.MingYi.kill.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    @RequestMapping(value = {"/to/login","/unAuth"})
    public String toLogin(){
        return "login";
    }


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public  String login(@RequestParam String userName, @RequestParam String password, ModelMap modelMap){
        String errorMsg = "";

        //TODO:shiro认证
        try {
            if(!SecurityUtils.getSubject().isAuthenticated()) {
                UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
                SecurityUtils.getSubject().login(token);
            }
        }catch (UnknownAccountException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("errorMsg",errorMsg);
        }catch (DisabledAccountException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("errorMsg",errorMsg);
        }catch (IncorrectCredentialsException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("errorMsg",errorMsg);
        }
        catch (Exception e){
            errorMsg = "用户登录异常";
            modelMap.addAttribute("errorMsg",errorMsg);
        }
        log.info(errorMsg);
        if(StringUtils.isBlank(errorMsg)){
            System.out.println("success");
            return "redirect:/index";
        }else {
            return "redirect:/to/login";
        }
    }
}
