package com.MingYi.kill.server.utils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成随机的ID
 */
public class RandomUtil {
    private static  final SimpleDateFormat dataFormaOne = new SimpleDateFormat("yyyyMMddHHmmssSS");

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();


    public  static  String generatOrderCode(){
        //TODO：基于时间戳
       return dataFormaOne.format(DateTime.now().toDate()) + generateNumber(4);
    }

    //随机数
    //TODO:考虑线程安全
    public static String generateNumber(final  int num){
        StringBuffer sb = new StringBuffer();
        for(int i = 1; i < num;i++){
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }
}
