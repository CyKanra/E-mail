package com.javapractice.zookeeper;

import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;

/**
 * @ClassName: CreateSession
 * @Description:
 * @Author: Kanra
 * @Date: 2023/10/19
 */
public class CreateSession {

    public static void main(String[] args) throws IOException {
        ZkClient zkClient = new ZkClient("192.168.31.131:2181");
        System.out.println("ZooKeeper session created");
    }
}
