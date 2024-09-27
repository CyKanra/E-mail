package com.javapractice.other;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @ClassName: ByteBufferApp
 * @Description:
 * @Author: Kanra
 * @Date: 2023/02/22
 */
public class ByteBufferApp {

    @Test
    public void testByteBuffer(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        System.out.println(byteBuffer);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 2);
        byteBuffer.put((byte) 3);
        System.out.println(byteBuffer);

        System.out.println("-----------------------------------------");
        byteBuffer.flip();
        System.out.println(byteBuffer);

        byteBuffer.get();
        byteBuffer.get();
        System.out.println(byteBuffer);
    }
}
