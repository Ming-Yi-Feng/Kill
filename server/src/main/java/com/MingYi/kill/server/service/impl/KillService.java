package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.model.entity.ItemKill;
import com.MingYi.kill.model.entity.ItemKillSuccess;
import com.MingYi.kill.model.entity.RandomCode;
import com.MingYi.kill.model.mapper.ItemKillMapper;
import com.MingYi.kill.model.mapper.ItemKillSuccessMapper;
import com.MingYi.kill.server.enums.SysConstant;
import com.MingYi.kill.server.service.IKillService;
import com.MingYi.kill.server.utils.RandomUtil;
import com.MingYi.kill.server.utils.SnowFlake;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KillService implements IKillService {

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSendMSG rabbitSendMSG;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private SnowFlake snowFlake = new SnowFlake(2,3);

    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        //TODO:判断当前用户是否已经抢过该商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //:TODO 查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
            //TODO: 查询是否可以秒杀
            if(itemKill !=null && 1 == itemKill.getCanKill()){
                //TODO: 数据库写的减一
                //TODO：这里高并发下面：这里就有问题，无锁，很多线程同时进入，并返回成功
                int res = itemKillMapper.updateKillItemV2(killId);
                if(res>0){
                    //在生成订单的时候，再次确认（但是数据库已经减一了）
                    if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0) {
                        commomRecordKillSuccessInfo(itemKill, userId);
                        result = true;
                    }
                }
            }
        }else{
            throw  new Exception("您已经抢过该商品了");
        }
        return result;
    }



    /**
     * Redis的分布式锁优化
     * SetNX 操作
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0) {
            //TODO:借助Redis的原子操作 实现分布式锁
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value = RandomUtil.generatOrderCode();

            //（1）可以使用原子操作,需要springboot2.0
            //Boolean flag = valueOperations.setIfAbsent(key, value);
            //valueOperations.set(key,value,30,TimeUnit.SECONDS);

            //(2) 集群类的Redis，使用Redisson
            RLock lock = redissonClient.getLock(key);
            try {
                Boolean flag = lock.tryLock(30,10,TimeUnit.SECONDS);
                if(flag) {
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill()) {
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if (res > 0) {
                            commomRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    }
                }else {
                    throw  new Exception("没抢到");
                }
            } finally {
                lock.unlock();
                //lock.forceUnlock();
            }
        }
        return result;
    }


    /**
     * 通用方法 -- 记录用户秒杀成功后生产的订单 -- 并进行异步的邮件消息服务
     */
    private void commomRecordKillSuccessInfo (ItemKill kill, Integer userId) throws Exception{
        //TODO: 记录抢购成功后生产的秒杀订单 到 success表里
        ItemKillSuccess success = new ItemKillSuccess();
        String orderNo = String.valueOf(snowFlake.nextId());
        //success.setCode(RandomUtil.generatOrderCode()); //传统时间戳 + 随机数
        success.setCode(orderNo);
        success.setItemId(kill.getItemId());
        success.setKillId(kill.getId());
        success.setUserId(userId.toString());
        success.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        success.setCreateTime(DateTime.now().toDate());


        //TODO:学以致用，举一反三 -> 仿照单例模式的双重检验锁写法,生成订单再次确认
        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId) <= 0) {
            int res = itemKillSuccessMapper.insertSelective(success);
            if (res > 0) {
                //TODO:进行异步通信，发邮件和信息 = rabbitMQ + 邮件
                rabbitSendMSG.sendKillSuccessEmailMsg(orderNo);

                //TODO：进入死信队列，用于超时后订单取消
                rabbitSendMSG.sendKillSuccessOrderExprieMsg(orderNo);
            }
        }
    }
}
