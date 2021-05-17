package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.model.entity.ItemKill;
import com.MingYi.kill.model.mapper.ItemKillMapper;
import com.MingYi.kill.model.mapper.ItemKillSuccessMapper;
import com.MingYi.kill.server.controller.ItemController;
import com.MingYi.kill.server.service.IItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService implements IItemService {
    private static final Logger log =  LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemKillMapper itemKillMapper;

    /**
     * 获取商品列表
     * @return
     * @throws Exception
     */
    @Override
    public List<ItemKill> getKillItems()  {
        List<ItemKill> res = itemKillMapper.selectAll();
        return res;
    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill entity = itemKillMapper.selectById(id);
        if(entity == null){
            throw new Exception("获取秒杀详情-待秒杀商品记录不存在");
        }
        return entity;
    }


}
