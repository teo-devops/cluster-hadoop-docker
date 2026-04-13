package nextret;

import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class AverageLengthToolRunner extends Configured implements Tool {

    public static class AvgMapper extends Mapper<Object, Text, Text, DoubleWritable> {
        private Text letter = new Text();
        private DoubleWritable length = new DoubleWritable();
        private boolean caseSensitive;

        @Override
        protected void setup(Context context) {
            caseSensitive = context.getConfiguration().getBoolean("caseSensitive", true);
        }

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] words = value.toString().split("\\s+");
            for (String word : words) {
                String cleaned = word.replaceAll("[^a-zA-Z]", "");
                if (!cleaned.isEmpty()) {
                    if (!caseSensitive) cleaned = cleaned.toLowerCase();
                    letter.set(String.valueOf(cleaned.charAt(0)));
                    length.set((double) cleaned.length());
                    context.write(letter, length);
                }
            }
        }
    }

    public static class AvgReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private DoubleWritable result = new DoubleWritable();
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double sum = 0;
            int count = 0;
            for (DoubleWritable val : values) { sum += val.get(); count++; }
            result.set(sum / count);
            context.write(key, result);
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        // --- DEBUG: Esto nos dirá la verdad en el terminal ---
        System.out.println("DEBUG: Argumentos recibidos tras ToolRunner: " + Arrays.toString(args));
        System.out.println("DEBUG: Cantidad de argumentos: " + args.length);

        if (args.length != 2) {
            System.err.println("Uso: AverageLengthToolRunner [-D caseSensitive=true|false] <input> <output>");
            return -1;
        }

        Job job = Job.getInstance(getConf(), "Average Word Length ToolRunner");
        job.setJarByClass(AverageLengthToolRunner.class);
        job.setMapperClass(AvgMapper.class);
        job.setReducerClass(AvgReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        // IMPORTANTE: Pasamos un Configuration nuevo para asegurar que lea los -D
        int res = ToolRunner.run(new Configuration(), new AverageLengthToolRunner(), args);
        System.exit(res);
    }
}