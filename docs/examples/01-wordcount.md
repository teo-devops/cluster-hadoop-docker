# Example 1 — Word Count

The "Hello World" of MapReduce. Counts how many times each word appears across all of Shakespeare's works.

## Dataset

`/datasets/shakespeare/` — ~6 MB of plain text (comedies, tragedies, histories, poems).

## Run

```bash
docker exec namenode hadoop jar /home/training/lib/wordcount.jar \
  nextret.WordCount \
  /datasets/shakespeare \
  /output/wordcount
```

## View Results

```bash
# Top 20 most frequent words
docker exec namenode hdfs dfs -cat /output/wordcount/part-r-00000 \
  | sort -k2 -rn | head -20
```

## What to Observe

- The Mapper emits `(word, 1)` for every word token in each line.
- The framework **shuffles and sorts** all pairs by key before the Reducer sees them.
- The Reducer simply sums the values for each word.

## Clean Up

```bash
docker exec namenode hdfs dfs -rm -r /output/wordcount
```

## Concepts Practiced

| Concept | Where |
|---|---|
| `Mapper<Object, Text, Text, IntWritable>` | Tokenisation |
| `Reducer<Text, IntWritable, Text, IntWritable>` | Summation |
| `FileInputFormat` / `FileOutputFormat` | I/O binding |
