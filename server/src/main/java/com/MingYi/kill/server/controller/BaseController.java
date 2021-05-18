package com.MingYi.kill.server.controller;


import com.MingYi.kill.api.enums.StatusCode;
import com.MingYi.kill.api.response.BaseResponse;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("base")
public class BaseController {

    //测试页面跳转
    @RequestMapping("/wecome")
    public String welcome(String parm, ModelMap modelMap) {
        if(StringUtils.isBlank(parm)){
            parm = "这是welcome页面哦!!!!";
        }
        modelMap.put("parm",parm);
        return "welcome";
    }

    //测试拿json数据
    @RequestMapping("/data")
    @ResponseBody
    public String data(String name) {
        if(StringUtils.isBlank(name)){
            name = "这是welcome页面哦!!!!";
        }
        return name;
    }

    //后台返回前端数据有一些规范
    /**
     * 这里需要用到api的枚举类了哦
     */
    @RequestMapping("/response")
    @ResponseBody
    public BaseResponse response(String name) {
        BaseResponse response = new BaseResponse(StatusCode.Success);
        if(StringUtils.isBlank(name)){
            name = "这是welcome页面哦!!!!";
        }
        response.setData(name);
        return response;
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public  String error(){
        return "error";
    }

}
