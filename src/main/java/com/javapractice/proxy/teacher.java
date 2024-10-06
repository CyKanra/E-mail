package com.javapractice.proxy;

/**
 * @ClassName: teacher
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/18
 */
public class teacher implements Person{
    @Override
    public void giveMoney(String name) {
        System.out.println(name +"60$払う");
    }
}
