package nextret;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Ejercicio 3: IpCount con Partitioner por mes
 *
 * El log tiene formato Common Log Format:
 *   10.216.113.172 - - [01/Jan/2003:01:31:00 -0500] "GET /..." 200 1234
 *                       ^^^^^^^^^^^^^^^^^^^
 *                       El mes está en la posición [3] del campo de fecha
 *
 * Hay 12 reducers (uno por mes). El Partitioner extrae el mes de la clave
 * compuesta "IP|mes" y devuelve el índice del reducer correspondiente (0-11).
 *
 * La salida son 12 ficheros part-r-00000 a part-r-00011, uno por mes.
 *
 * Uso:
 *   hadoop jar ipcountpartitioner.jar nextret.IpCountPartitioner /weblog /output/ipcount_part
 */
public class IpCountPartitioner {

    // Mapa de nombre de mes (abreviatura inglesa) → índice 0-11
    private static final Map<String, Integer> MONTH_INDEX = new HashMap<>();
    static {
        MONTH_INDEX.put("Jan", 0);  MONTH_INDEX.put("Feb", 1);
        MONTH_INDEX.put("Mar", 2);  MONTH_INDEX.put("Apr", 3);
        MONTH_INDEX.put("May", 4);  MONTH_INDEX.put("Jun", 5);
        MONTH_INDEX.put("Jul", 6);  MONTH_INDEX.put("Aug", 7);
        MONTH_INDEX.put("Sep", 8);  MONTH_INDEX.put("Oct", 9);
        MONTH_INDEX.put("Nov", 10); MONTH_INDEX.put("Dec", 11);
    }

    // -------------------------------------------------------------------------
    // MAPPER
    // Emite clave compuesta "IP|mes" y valor 1
    // Ejemplo: "10.216.113.172|Jan"  →  1
    // -------------------------------------------------------------------------
    public static class IpMonthMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text ipMonth = new Text();

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString().trim();
            if (line.isEmpty()) return;

            String[] parts = line.split("\\s+");
            // parts[0] = IP
            // parts[3] = [01/Jan/2003:01:31:00  → quitar el '[' inicial
            if (parts.length < 4) return;

            String ip = parts[0];
            String dateField = parts[3].replaceAll("[\\[\\]]", ""); // "01/Jan/2003:01:31:00"
            String[] dateParts = dateField.split("/");              // ["01","Jan","2003:01:31:00"]
            if (dateParts.length < 2) return;

            String month = dateParts[1]; // "Jan", "Feb", ...
            if (!MONTH_INDEX.containsKey(month)) return;

            ipMonth.set(ip + "|" + month);
            context.write(ipMonth, one);
        }
    }

    // -------------------------------------------------------------------------
    // PARTITIONER
    // Extrae el mes de la clave "IP|mes" y devuelve el índice del reducer
    // -------------------------------------------------------------------------
    public static class MonthPartitioner extends Partitioner<Text, IntWritable> {

        @Override
        public int getPartition(Text key, IntWritable value, int numPartitions) {
            String[] parts = key.toString().split("\\|");
            if (parts.length < 2) return 0;
            String month = parts[1];
            Integer idx = MONTH_INDEX.get(month);
            return (idx != null) ? idx % numPartitions : 0;
        }
    }

    // -------------------------------------------------------------------------
    // REDUCER
    // Suma los conteos para cada "IP|mes"
    // -------------------------------------------------------------------------
    public static class IpMonthReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) sum += val.get();
            result.set(sum);
            context.write(key, result);
        }
    }

    // -------------------------------------------------------------------------
    // DRIVER
    // -------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: IpCountPartitioner <input> <output>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "IP Count by Month (Partitioner)");

        job.setJarByClass(IpCountPartitioner.class);
        job.setMapperClass(IpMonthMapper.class);
        job.setPartitionerClass(MonthPartitioner.class);   // Partitioner personalizado
        job.setReducerClass(IpMonthReducer.class);
        job.setNumReduceTasks(12);                         // Un reducer por mes

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}