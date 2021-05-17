package com.MingYi.kill.server.service;

public interface IKillService {
    Boolean killItem(Integer killId,Integer userId) throws Exception;

    Boolean killItemV2(Integer killId,Integer userId) throws Exception;
}
