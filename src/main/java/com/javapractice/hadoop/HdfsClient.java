package com.javapractice.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @ClassName: HdfsClient
 * @Description:
 * @Author: Kanra
 * @Date: 2023/01/16
 */
public class HdfsClient {

        private FileSystem fs = null;

        @Before
        public void start() throws URISyntaxException, IOException, InterruptedException{
            Configuration configuration = new Configuration();
            configuration.set("fs.defaultFS", "hdfs://centos1:9000");
            fs = FileSystem.get(new URI("hdfs://centos1:9000"), configuration, "root");
        }

        @Test
        public void testMkdirs() throws IOException {
            fs.copyFromLocalFile(new Path("D:/log_info.log"), new Path("/bigdata1"));
        }

        @After
        public void end() throws IOException {
            fs.close();
        }

}
