package com.javapractice.proxy;

/**
 * @ClassName: StaticProxyTest
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/16
 */
public class StaticProxyTest {

    public static void main(String[] args) {
//        Person person = new StudentsProxy(new Student());
//        person.giveMoney("kanra");
        SmsService smsService = (SmsService) new ProxyFactory().getProxy(new SmsServiceImpl());
        System.out.println("smsService====:"+smsService.getClass().getName());
        smsService.send("java");
    }
}
