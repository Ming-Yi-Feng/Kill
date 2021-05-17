package com.MingYi.kill.server.service.impl;

import com.MingYi.kill.server.Dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class MailService {
    private static final Logger log =  LoggerFactory.getLogger(RabbitSendMSG.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;
    /**
     * 发送简单文本邮件
     */
    @Async
    public void sendSimpleEmail(final MailDto dto){
        //一封正常的邮件有标题，内容，发送人，收件人，时间 -- 封装成一个对象
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(env.getProperty("mail.send.from"));
            message.setTo(dto.getTos());
            message.setSubject(dto.getSubject());
            message.setText(dto.getContent());
            mailSender.send(message);

            log.info("发送简单文本文件 - 成功");
        }catch (Exception e){
            log.error("发送简单文本文件 - 异常： ",e.fillInStackTrace());
        }
    }

    /**
     * 发送多媒体文本邮件
     */
    @Async
    public void sendHtmlEmail(final MailDto dto){
        //一封正常的邮件有标题，内容，发送人，收件人，时间 -- 封装成一个对象
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,true,"utf-8");
            mimeMessageHelper.setFrom(env.getProperty("mail.send.from"));
            mimeMessageHelper.setTo(dto.getTos());
            mimeMessageHelper.setSubject(dto.getSubject());
            mimeMessageHelper.setText(dto.getContent(),true);
            mailSender.send(message);
            log.info("发送花哨邮件 - 成功");

        }catch (Exception e){
            log.error("发送花哨邮件 - 异常： ",e.fillInStackTrace());
        }
    }
}
