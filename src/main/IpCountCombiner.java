package nextret;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Ejercicio 2: IpCount con Combiner
 *
 * El Combiner ejecuta una reducción parcial local en cada nodo Map
 * antes del shuffle, reduciendo drásticamente el volumen de datos
 * transferidos por la red.
 *
 * Como la operación es una suma (asociativa y conmutativa), el mismo
 * IpReducer puede usarse como Combiner sin modificaciones.
 *
 * Uso:
 *   hadoop jar ipcountcombiner.jar nextret.IpCountCombiner /weblog /output/ipcount_combiner
 */
public class IpCountCombiner {

    // -------------------------------------------------------------------------
    // MAPPER — igual que IpCount original
    // -------------------------------------------------------------------------
    public static class IpMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text ip = new Text();

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0) {
                    ip.set(parts[0]);
                    context.write(ip, one);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // REDUCER — también actúa como COMBINER (suma es asociativa)
    // -------------------------------------------------------------------------
    public static class IpReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    // -------------------------------------------------------------------------
    // DRIVER — añade setCombinerClass
    // -------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: IpCountCombiner <input> <output>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "IP Count with Combiner");

        job.setJarByClass(IpCountCombiner.class);
        job.setMapperClass(IpMapper.class);
        job.setCombinerClass(IpReducer.class);   // <-- ÚNICA DIFERENCIA respecto a IpCount
        job.setReducerClass(IpReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}