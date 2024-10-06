package com.javapractice.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

/**
 * @ClassName: WordCountMap
 * @Description:
 * @Author: Kanra
 * @Date: 2023/02/20
 */
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    Text text = new Text();
    IntWritable intWritable = new IntWritable(1);
    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        //一行文字を取得
        String str = value.toString();
        System.out.println("======>"+str);
        //文字を区切って単語数組になる
        String[] strs = str.split(" ");
        //出力
        for (String s : strs) {
            text.set(s);
            context.write(text, intWritable);
        }
    }
}
