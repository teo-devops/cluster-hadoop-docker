# Example 4 — IP Count with Custom Partitioner

Extends Example 3 by adding a **custom Partitioner** that controls which Reducer receives each IP address.

## Why Custom Partitioners?

By default, Hadoop uses `HashPartitioner`, which distributes keys across reducers using a hash function. This works well for uniform data but can cause **data skew** — one reducer ends up with far more keys than others.

A custom Partitioner lets you:
- Route keys to specific reducers intentionally
- Balance load more evenly
- Group related keys on the same reducer (e.g., all IPs from the same subnet)

## This Example's Strategy

IPs are partitioned by their **first octet** (subnet range):

| First Octet Range | Reducer |
|---|---|
| 0–63 | Reducer 0 |
| 64–127 | Reducer 1 |
| 128–191 | Reducer 2 |
| 192–255 | Reducer 3 |

## Run

```bash
docker exec namenode hadoop jar /home/training/lib/ipcountpart.jar \
  /datasets/weblog \
  /output/ipcount_partitioner
```

## View Results

With a custom Partitioner and multiple reducers, you get **multiple output files**:

```bash
docker exec namenode hdfs dfs -ls /output/ipcount_partitioner
# → part-r-00000, part-r-00001, part-r-00002, part-r-00003

# View each partition
docker exec namenode hdfs dfs -cat /output/ipcount_partitioner/part-r-00000 | head -5
docker exec namenode hdfs dfs -cat /output/ipcount_partitioner/part-r-00001 | head -5
```

## Clean Up

```bash
docker exec namenode hdfs dfs -rm -r /output/ipcount_partitioner
```

## Concepts Practiced

| Concept | Where |
|---|---|
| `Partitioner<Text, IntWritable>` | Custom class extending Partitioner |
| `job.setPartitionerClass(...)` | Registration |
| `job.setNumReduceTasks(4)` | Multiple output files |
| Data skew awareness | Subnet-based routing |
