# Example 2 — IP Count

Counts how many HTTP requests each IP address made, using a real Apache access log.

## Dataset

`/datasets/weblog/access_log.gz` — ~200 MB of Apache Combined Log Format entries.  
Hadoop reads `.gz` files natively — no decompression needed.

## Run

```bash
docker exec namenode hadoop jar /home/training/lib/ipcount.jar \
/datasets/weblog \
/output/ipcount
```

## View Results

```bash
# Top 10 IPs by request count
docker exec namenode hdfs dfs -cat /output/ipcount/part-r-00000 \
  | sort -k2 -rn | head -10
```

Expected output format:
```
10.0.0.1    3842
192.168.1.5 2901
...
```

## How It Works

The Mapper parses each log line and extracts the **first field** (the client IP):

```
66.249.71.1 - - [01/Jan/2013:00:00:12 +0100] "GET /..." 200 5765
^^^^^^^^^
emits: (66.249.71.1, 1)
```

The Reducer sums all counts per IP.

## Clean Up

```bash
docker exec namenode hdfs dfs -rm -r /output/ipcount
```

## Concepts Practiced

| Concept | Where |
|---|---|
| Parsing structured log lines | `IpMapper.map()` |
| `String.split("\\s+")` field extraction | Line tokenisation |
| Reading compressed input | `.gz` handled transparently |

## See Also

- [Example 3 — IP Count with Combiner](03-ipcount-combiner.md) — same logic, less network I/O
- [Example 4 — IP Count with Partitioner](04-ipcount-partitioner.md) — control reducer assignment
