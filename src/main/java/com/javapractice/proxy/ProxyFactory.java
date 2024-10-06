package com.javapractice.proxy;

import java.lang.reflect.Proxy;

/**
 * @ClassName: ProxyFactory
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/23
 */
public class ProxyFactory {

    public Object getProxy(Object target) {

        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(), // 目标类的类加载器
                target.getClass().getInterfaces(),  // 代理需要实现的接口，可指定多个
                new SendInvocationHandler(target)   // 代理对象对应的自定义 InvocationHandler
        );
    }

}
