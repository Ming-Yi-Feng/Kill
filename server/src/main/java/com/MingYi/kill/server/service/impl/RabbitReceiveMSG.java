package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.model.dto.KillSuccessUserInfo;
import com.MingYi.kill.model.entity.ItemKillSuccess;
import com.MingYi.kill.model.mapper.ItemKillSuccessMapper;
import com.MingYi.kill.server.Dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitReceiveMSG {
    private static final Logger log =  LoggerFactory.getLogger(RabbitSendMSG.class);

    @Autowired
    private Environment env;
    @Autowired
    private MailService mailService;
    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    //设置监听器,单一消费者实例，监听的队列名也在spring里
    //消费者可以设置
    @RabbitListener(queues = "${mq.kill.item.success.email.queue}",containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
        try{
            log.info("秒杀异步邮件通知 - 接收消息{}",info);

            //TODO:发送邮件。
            String content = String.format(env.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());

            MailDto dto = new MailDto(env.getProperty("mail.kill.item.success.subject"),content,new String[]{info.getEmail()});
            mailService.sendHtmlEmail(dto);

        }catch (Exception  e){
            log.error("秒杀异步邮件通知 - 接收消息 - 发生异常",e.fillInStackTrace());
        }
    }

    /**
     * 超时未支付的消息，就到这里
     * @param info
     */
    @RabbitListener(queues = "${mq.kill.item.success.kill.dead.real.queue}",containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try{
            log.error("用户秒杀成功后超时未支付 - 监听者 - 接收消息{}",info);
            //TODO:再次查看订单状态
            if(info!=null){
                ItemKillSuccess entity = itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
                //订单还是为0
                if(entity!=null && entity.getStatus().intValue() == 0){
                    itemKillSuccessMapper.expireOrder(info.getCode());
                    //没有还原？？
                }
            }
        }catch (Exception  e){
            log.error("用户秒杀成功后超时未支付 - 监听者 - 发生异常",e.fillInStackTrace());
        }
    }
}
