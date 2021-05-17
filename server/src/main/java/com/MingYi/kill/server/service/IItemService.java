package com.MingYi.kill.server.service;

import com.MingYi.kill.model.entity.Item;
import com.MingYi.kill.model.entity.ItemKill;

import java.util.List;

public interface IItemService {

    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id) throws Exception;
}
