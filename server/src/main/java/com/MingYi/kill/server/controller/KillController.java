package com.MingYi.kill.server.controller;

import com.MingYi.kill.api.enums.StatusCode;
import com.MingYi.kill.api.response.BaseResponse;
import com.MingYi.kill.model.dto.KillSuccessUserInfo;
import com.MingYi.kill.model.mapper.ItemKillSuccessMapper;
import com.MingYi.kill.server.Dto.KillDto;
import com.MingYi.kill.server.service.impl.KillService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class KillController {

    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    @Autowired
    private KillService killService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    //跳转页面
    @RequestMapping(value = prefix+"/executeSuccess",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    @RequestMapping(value = prefix+"/executeFail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }


    /**
     * 秒杀业务
     * @param dto
     * @param result
     * @param session
     * @return
     */
    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //BindingResult是验证输入合理性的类 -- 与 Valid配合使用
    public BaseResponse execute(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session) {
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {

            Boolean res=killService.killItem(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

    @RequestMapping(value = prefix + "/record/detail/{orderNo}",method = RequestMethod.GET)
    public String killRecordDetail(@PathVariable String orderNo, ModelMap map){
        if(StringUtils.isNotBlank(orderNo)) {
            KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
            if (info == null){
                return "error";
            }
            map.put("info",info);
        }
        return "killRecord";
    }


    /**
     * 秒杀业务
     * @param dto
     * @param result
     * @param session
     * @return
     */
    @RequestMapping(value = prefix+"/execute/lock",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //BindingResult是验证输入合理性的类 -- 与 Valid配合使用
    public BaseResponse executeLock(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session) {
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            //不加分布式锁的前提
            Boolean res=killService.killItemV2(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

}
