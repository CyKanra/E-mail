package com.javapractice.proxy;

/**
 * @ClassName: SmsServiceImpl
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/23
 */
public class SmsServiceImpl implements SmsService{
    @Override
    public String send(String message) {
        System.out.println("send Smsmessage:" + message);
        return message;
    }
}
