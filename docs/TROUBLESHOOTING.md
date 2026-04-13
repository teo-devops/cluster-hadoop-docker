# Troubleshooting

## Cluster won't start

```bash
# Check all containers are running
docker compose ps

# View logs for a specific service
docker compose logs namenode
docker compose logs resourcemanager
```

## HDFS shows 0 DataNodes

The DataNode needs a moment to register. Wait 30s then check:

```bash
docker exec namenode hdfs dfsadmin -report
```

If it still shows 0 DataNodes:
```bash
docker compose restart datanode
```

## "Connection refused" on port 8888 (Hue)

Hue depends on Hive being fully ready. Wait ~2 minutes after startup, or:

```bash
docker compose logs hue | tail -20
docker compose restart hue
```

## MapReduce job fails with "Container killed"

Your Docker VM doesn't have enough memory. Edit `hadoop.env`:

```env
# Reduce these values if RAM is limited
YARN_CONF_yarn_nodemanager_resource_memory_mb=1536
YARN_CONF_yarn_scheduler_maximum_allocation_mb=768
MAPRED_CONF_mapreduce_map_memory_mb=384
MAPRED_CONF_mapreduce_reduce_memory_mb=384
```

Then restart: `docker compose down && docker compose up -d`

## Output directory already exists

Hadoop refuses to overwrite output. Delete the old output first:

```bash
docker exec namenode hdfs dfs -rm -r /output/YOUR_OUTPUT_DIR
```

## Reset everything

```bash
docker compose down -v   # removes HDFS data volumes too
docker compose up -d
./sh/init-hdfs.sh
```
