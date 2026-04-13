# Example 5 — Average Word Length per Initial Letter

Computes the **average length of words** grouped by their first letter, across any text corpus. Uses the `ToolRunner` pattern for clean configuration management.

## Dataset

Works with any text — try Shakespeare or the Bible:
- `/datasets/shakespeare`
- `/datasets/bible`

## Run

### Case-insensitive (recommended)

```bash
docker exec namenode hadoop jar /home/training/lib/avglength.jar \
  nextret.AverageLengthToolRunner \
  -D caseSensitive=false \
  /datasets/shakespeare \
  /output/avglength
```

### Case-sensitive

```bash
docker exec namenode hadoop jar /home/training/lib/avglength.jar \
  nextret.AverageLengthToolRunner \
  -D caseSensitive=true \
  /datasets/shakespeare \
  /output/avglength_cs
```

## View Results

```bash
docker exec namenode hdfs dfs -cat /output/avglength/part-r-00000
```

Expected output:
```
a    4.21
b    5.83
c    6.10
...
z    5.67
```

## Try with Different Datasets

```bash
# Compare Shakespeare vs Bible
docker exec namenode hadoop jar /home/training/lib/avglength.jar \
  nextret.AverageLengthToolRunner \
  -D caseSensitive=false \
  /datasets/bible \
  /output/avglength_bible

docker exec namenode hdfs dfs -cat /output/avglength_bible/part-r-00000
```

## The ToolRunner Pattern

`ToolRunner` is the idiomatic way to write configurable Hadoop jobs:

```java
// Allows passing -D key=value flags from the command line
int res = ToolRunner.run(new Configuration(), new AverageLengthToolRunner(), args);
```

Without ToolRunner, `-D` flags are parsed by the shell but never reach the job's `Configuration` object.

## Key Design Choices

| Decision | Reason |
|---|---|
| Key = first letter | Groups words by initial character |
| Value = `DoubleWritable` | Needed for floating-point averages |
| Reducer computes `sum / count` | Average cannot use a Combiner (non-associative) |
| `word.replaceAll("[^a-zA-Z]", "")` | Strips punctuation before measurement |

## Clean Up

```bash
docker exec namenode hdfs dfs -rm -r /output/avglength /output/avglength_cs /output/avglength_bible
```

## Concepts Practiced

| Concept | Where |
|---|---|
| `ToolRunner` / `Tool` interface | Clean `-D` flag handling |
| `Configured` base class | Automatic config propagation |
| `DoubleWritable` | Floating-point output type |
| Non-trivial Reducer logic | Average (not just sum) |
| Runtime configuration | `caseSensitive` parameter |
