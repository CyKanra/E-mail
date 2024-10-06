package com.javapractice.hadoop;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @ClassName: Student
 * @Description:
 * @Author: Kanra
 * @Date: 2024/08/22
 */
public class Student implements Writable {

    private Long id;
    private String name;
    private Integer age;

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(id);
        dataOutput.writeUTF(name);
        dataOutput.writeInt(age);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.id = dataInput.readLong();
        this.name = dataInput.readUTF();
        this.age = dataInput.readInt();
    }
}
