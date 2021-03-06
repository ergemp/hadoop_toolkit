package org.ergemp.toolkit.hadoop.training.MapreduceExamples;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;

public class LineCountExample {
    public static void main(String[] args) {
        try {
            Configuration cconf = new Configuration();
            cconf.set("fs.defaultFS", "hdfs://127.0.0.1:8020");
            cconf.set("mapreduce.framework.name", "local");
            cconf.set("yarn.resourcemanager.address", "127.0.0.1:8032");
            //conf.set("mapreduce.job.running.map.limit", "3");
            //conf.set("mapreduce.job.maps", "3");

            //20190924: due to java.io.IOException: No FileSystem for scheme: hdfs
            cconf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            cconf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

            // Create job
            Job job = Job.getInstance(cconf, "LineCountExample");
            job.setJarByClass(LineCountExample.class);

            job.setMapperClass(LineCountExample.Map.class);
            job.setReducerClass(LineCountExample.Reduce.class);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(IntWritable.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);

            // Input
            FileInputFormat.addInputPath(job, new Path(args[0]));
            job.setInputFormatClass(TextInputFormat.class);

            // Output
            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            job.setOutputFormatClass(TextOutputFormat.class);

            // Execute job and return status
            //return job.waitForCompletion(true) ? 0 : 1;
            job.waitForCompletion(true);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    //Mapper Class
    public static class Map extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            //StringTokenizer itr = new StringTokenizer(line, " ");

            //while (itr.hasMoreTokens()) {
                word.set("");
                context.write(word, one);
            //}
        }
    }//Mapper Class

    //Reducer Class
    public static class Reduce extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            Integer sum = 0;

            Iterator valuesIt = values.iterator();
            while(valuesIt.hasNext()) {
                sum += Integer.parseInt(valuesIt.next().toString());
            }

            result.set(sum);
            context.write(key, result);
        }
    }//Reducer Class
}
