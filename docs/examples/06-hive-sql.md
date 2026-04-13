# Hive SQL Examples

Run SQL queries on HDFS data using Apache Hive.

## Connect to Hive

```bash
docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000
```

Or use **Hue** at http://localhost:8888 → Query Editor → Hive.

---

## Example A — Word Frequency Table from Shakespeare

```sql
-- Create external table pointing to HDFS path
CREATE EXTERNAL TABLE IF NOT EXISTS shakespeare_lines (
  line STRING
)
STORED AS TEXTFILE
LOCATION '/datasets/shakespeare';

-- Word frequency
SELECT word, COUNT(*) AS freq
FROM (
  SELECT explode(split(lower(line), '\\s+')) AS word
  FROM shakespeare_lines
) w
WHERE word REGEXP '^[a-z]+$'
GROUP BY word
ORDER BY freq DESC
LIMIT 20;
```

---

## Example B — Top IPs from Web Log

First, load the access log into a Hive table:

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS access_log (
  ip            STRING,
  ident         STRING,
  user          STRING,
  request_time  STRING,
  request       STRING,
  status        INT,
  bytes         INT,
  referer       STRING,
  agent         STRING
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.RegexSerDe'
WITH SERDEPROPERTIES (
  "input.regex" = '(\\S+) (\\S+) (\\S+) \\[([^\\]]+)\\] "([^"]+)" (\\d{3}) (\\S+) "([^"]*)" "([^"]*)"'
)
STORED AS TEXTFILE
LOCATION '/datasets/weblog';

-- Top 10 IPs
SELECT ip, COUNT(*) AS requests
FROM access_log
GROUP BY ip
ORDER BY requests DESC
LIMIT 10;

-- HTTP status code distribution
SELECT status, COUNT(*) AS total
FROM access_log
GROUP BY status
ORDER BY total DESC;

-- 404 errors
SELECT ip, request, COUNT(*) AS hits
FROM access_log
WHERE status = 404
GROUP BY ip, request
ORDER BY hits DESC
LIMIT 10;
```

---

## Tips

- Hive jobs run as MapReduce under the hood — watch progress in the YARN UI at `:8088`
- Use `EXPLAIN <query>` to see the execution plan
- Add `SET hive.exec.reducers.bytes.per.reducer=67108864;` to tune reducer count
