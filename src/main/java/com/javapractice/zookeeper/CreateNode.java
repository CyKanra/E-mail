package com.javapractice.zookeeper;

import org.I0Itec.zkclient.ZkClient;

/**
 * @ClassName: CreateNode
 * @Description:
 * @Author: Kanra
 * @Date: 2023/10/20
 */
public class CreateNode {

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("192.168.31.131:2181");
        System.out.println("ZooKeeper session established.");
        zkClient.createPersistent("/znode-zkClient/znode1",true);
        System.out.println("success create znode.");
    }
}
