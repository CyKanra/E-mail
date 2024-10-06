package com.javapractice.proxy;

/**
 * @ClassName: StudentsProxy
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/15
 */
public class StudentsProxy implements Person{

     Person person;

    StudentsProxy(Person per){
        this.person = per;
    }
    public void giveMoney(String name) {
        System.out.println("新しい機能を加える");
        person.giveMoney(name);
    }
}
