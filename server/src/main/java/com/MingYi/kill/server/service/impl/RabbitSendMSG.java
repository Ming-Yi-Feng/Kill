package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.model.dto.KillSuccessUserInfo;
import com.MingYi.kill.model.mapper.ItemKillSuccessMapper;
import com.MingYi.kill.server.controller.ItemController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitSendMSG {
    private static final Logger log =  LoggerFactory.getLogger(RabbitSendMSG.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 秒杀成功发送邮件
     */
    public void sendKillSuccessEmailMsg(String orderNo){
        log.info("秒杀成功,准备异步发送邮件，{}",orderNo);

        try{
            if(StringUtils.isNotBlank(orderNo)){
                KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
                if(info != null){
                    //TODO: 发送邮件消息给MQ, 绑定rabbitMQ的参数
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.email.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.email.routing.key"));

                    //TODO: 对象当作消息
                    rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            //消息持久化
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_KEY_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            return message;
                        }
                    });
                }
            }

        }catch (Exception e){
            log.error("秒杀成功，但发送邮件异常" + orderNo,e.fillInStackTrace());
        }
    }

    /**
     * 秒杀成功后生成订单 -- 发送信息入死信队列，等待一定时间失效取消订单
     */
    public void sendKillSuccessOrderExprieMsg(final String orderCode){
        try{
            if(StringUtils.isNotBlank(orderCode)){
                KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderCode);
                if(info!=null){
                    //通过基本交换机路由到死信队列
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.kill.dead.prod.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.kill.dead.prod.routing.key"));
                    rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_KEY_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);

                            //设置TTL
                            //死信队列超时后自动转发给死信交换机，然后死信交换机发给绑定了的真正队列
                            messageProperties.setExpiration(env.getProperty("mq.kill.item.success.kill.expire"));
                            return message;
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error("秒杀成功，但发送消息到死信队列异常{}" + orderCode,e.fillInStackTrace());
        }
    }

}
