package com.MingYi.kill.server.controller;

import com.MingYi.kill.model.entity.ItemKill;
import com.MingYi.kill.server.service.impl.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 待秒杀商品的
 */
@Controller
public class ItemController {
    private static final Logger log =  LoggerFactory.getLogger(ItemController.class);
    private static final String prefix = "item";

    @Autowired
    private ItemService itemService;

    /**
     * 商品列表
     * @param modelMap
     * @return
     */
    @RequestMapping(value = {prefix+"/list","/index"},method = RequestMethod.GET)
    public String list(ModelMap modelMap){
        //获取待秒杀商品列表
        try{
            //获取待秒杀商品列表
            List<ItemKill> list =  itemService.getKillItems();
            modelMap.put("list",list);
        }catch (Exception e){
            log.error("获取秒杀商品列表异常",e.fillInStackTrace());
            return "redirect:/base/error";
        }
        return "list";
    }

    /**
     * 获取详情页
     */
    @RequestMapping(value = prefix+"/detail/{id}",method = RequestMethod.GET)
    public String detail(@PathVariable Integer id,ModelMap modelMap){
        if(id == null || id <=0) return "redirect:/base/error";
        try {
            ItemKill detail = itemService.getKillDetail(id);
            modelMap.put("detail",detail);
        }catch (Exception e){
            log.error("获取详情页异常: id = {}",id,e.fillInStackTrace());
            return "redirect:/base/error";
        }
        return "info";
    }

}
