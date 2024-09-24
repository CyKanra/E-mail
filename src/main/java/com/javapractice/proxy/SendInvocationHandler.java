package com.javapractice.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @ClassName: SendInvocationHandler
 * @Description:
 * @Author: Kanra
 * @Date: 2024/07/23
 */
public class SendInvocationHandler implements InvocationHandler {

    private final Object target;

    public SendInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before proxy:" + proxy.getClass());
        System.out.println("before method:" + method.getName());

        Object result = method.invoke(target, args);

        System.out.println("args:" + args);
        return result;
    }
}
