# Example 3 — IP Count with Combiner

Same as Example 2, but adds a **Combiner** to perform local pre-aggregation on each mapper node before data is sent over the network.

## Why Combiners Matter

In a real cluster with hundreds of mappers, the shuffle phase transfers massive amounts of intermediate data across the network. A Combiner runs **locally on the mapper node** and reduces that data before it leaves.

```
Without Combiner:            With Combiner:
Mapper → (ip, 1) x 1000  →  Mapper → (ip, 1000) x 1  → Reducer
                              ^
                         local mini-reduce
```

> **Rule of thumb:** Use a Combiner only when the operation is **associative and commutative** (sum, max, min — but NOT average).

## Run

```bash
docker exec namenode hadoop jar /home/training/lib/ipcountcombiner.jar \
  /datasets/weblog \
  /output/ipcount_combiner
```

## Compare with Example 2

Run both jobs and compare the **Map output records** counter in the job logs:

```bash
# Check ResourceManager UI → application → counters
# Look for: "Map output records" — should be much lower with the Combiner
```

Or inspect YARN logs:
```bash
docker exec resourcemanager yarn logs -applicationId <APP_ID> 2>/dev/null | grep "Map output records"
```

## View Results

```bash
docker exec namenode hdfs dfs -cat /output/ipcount_combiner/part-r-00000 \
  | sort -k2 -rn | head -10
```

Results should be **identical** to Example 2 — different performance, same output.

## Clean Up

```bash
docker exec namenode hdfs dfs -rm -r /output/ipcount_combiner
```

## Concepts Practiced

| Concept | Where |
|---|---|
| `job.setCombinerClass(IpReducer.class)` | Combiner registration |
| Local aggregation before shuffle | Reduced network traffic |
| Idempotency requirement | Why Reducer can double as Combiner |
