# 🐘 Hadoop MapReduce Lab — Docker Cluster

A fully functional **Hadoop 3.2.1 cluster** running on Docker, ready to experiment with MapReduce programming patterns using real datasets.

> **Stack:** HDFS · YARN · MapReduce · Hive · Hue  
> **Java:** 8 · **Hadoop:** 3.2.1 · **Hive:** 2.3.2

---

## 📦 What's Included

| Component | Purpose |
|---|---|
| **NameNode** | HDFS metadata, Web UI at `:9870` |
| **DataNode** | HDFS storage |
| **ResourceManager** | YARN job scheduling, Web UI at `:8088` |
| **NodeManager** | YARN task execution |
| **Hive + Metastore** | SQL-on-Hadoop via HiveQL |
| **Hue** | Web UI for HDFS & Hive at `:8888` |

---

## 🚀 Quick Start

### 1. Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (≥ 4.x)
- At least **4 GB RAM** allocated to Docker
- Ports **9870, 9000, 8088, 8888, 10000** free

### 2. Start the cluster

```bash
git clone https://github.com/YOUR_USER/hadoop-lab.git
cd hadoop-lab
```
```bash
docker compose up -d
```

Wait ~60 seconds for all services to initialize.

### 3. Load datasets into HDFS

```bash
chmod +x sh/init-hdfs.sh
./sh/init-hdfs.sh
```

This uploads Shakespeare texts and the Apache access log to HDFS.

### 4. Verify

| URL | What you'll see |
|---|---|
| http://localhost:9870 | HDFS NameNode UI |
| http://localhost:8088 | YARN ResourceManager |
| http://localhost:8888 | Hue (user: `admin`, pass: `admin`) |

---

## 📂 Project Structure

```
hadoop-lab/
├── docker-compose.yaml      # Cluster definition
├── hadoop.env               # Hadoop/YARN/Hive configuration
├── sh/
│   └── init-hdfs.sh         # Load datasets into HDFS
├── dataset/
│   ├── shakespeare.tar.gz   # Shakespeare complete works (~6 MB)
│   ├── shakespeare-stream.tar.gz
│   ├── bible.tar.gz         # King James Bible (~4 MB)
│   └── access_log.gz        # Apache web server log (~200 MB)
├── lib/
│   ├── wordcount.jar        # Example 1: Word Count
│   ├── ipcount.jar          # Example 2: IP Count
│   ├── ipcountcombiner.jar  # Example 3: IP Count with Combiner
│   ├── ipcountpart.jar      # Example 4: IP Count with Partitioner
│   └── avglength.jar        # Example 5: Average Word Length
├── src/main/                # Java source code for all examples
└── docs/
    └── examples/            # Step-by-step guides per use case
```

---

## 🧪 MapReduce Examples

See [`docs/examples/`](docs/examples/) for full walkthroughs:

| # | Example | Dataset | Concept |
|---|---|---|---|
| 1 | [Word Count](docs/examples/01-wordcount.md) | Shakespeare | Basic MapReduce |
| 2 | [IP Count](docs/examples/02-ipcount.md) | Access log | Text parsing |
| 3 | [IP Count + Combiner](docs/examples/03-ipcount-combiner.md) | Access log | Local aggregation |
| 4 | [IP Count + Partitioner](docs/examples/04-ipcount-partitioner.md) | Access log | Custom partitioning |
| 5 | [Average Word Length](docs/examples/05-avg-word-length.md) | Shakespeare / Bible | Aggregation + ToolRunner |

---

## 🛑 Stop the cluster

```bash
docker compose down

# To also delete HDFS data volumes:
docker compose down -v
```

---

## 📄 License

MIT — free to use, fork, and adapt for learning purposes.
