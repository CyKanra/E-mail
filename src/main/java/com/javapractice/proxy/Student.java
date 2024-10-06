package com.javapractice.proxy;

/**
 * @ClassName: student
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/15
 */
public class Student implements Person{
    @Override
    public void giveMoney(String name) {
        System.out.println(name + "50$払う");
    }
}
