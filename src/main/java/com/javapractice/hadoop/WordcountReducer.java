package com.javapractice.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @ClassName: WordcountReducer
 * @Description:
 * @Author: Kanra
 * @Date: 2023/02/23
 */
public class WordcountReducer extends Reducer<Text, IntWritable, Text,
        IntWritable> {

    int sum;

    IntWritable intWritable = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {

        sum = 0;
        System.out.println("key--------->"+key);
        System.out.println("values--------->"+values);
        System.out.println("context--------->"+context);
        for (IntWritable value : values) {
            sum += value.get();
        }
        intWritable.set(sum);
    }
}
