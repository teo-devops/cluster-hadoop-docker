#!/bin/bash
# =============================================================================
#  init-hdfs.sh — Load datasets into HDFS
#
#  Run this script AFTER `docker compose up -d` to upload the local
#  datasets into the Hadoop Distributed File System.
#
#  Usage:
#    chmod +x sh/init-hdfs.sh
#    ./sh/init-hdfs.sh
# =============================================================================

set -e

echo "⏳ Waiting for NameNode to be ready..."
until docker exec namenode hdfs dfs -ls / > /dev/null 2>&1; do
  echo "   ... still waiting"
  sleep 5
done
echo "✅ NameNode is ready."

# ─── Shakespeare (text corpus) ────────────────────────────────────────────────
echo ""
echo "📂 Loading Shakespeare corpus..."
docker exec namenode bash -c "
  cd /tmp && \
  tar -xzf /home/training/training_materials/developer/data/shakespeare.tar.gz && \
  hdfs dfs -mkdir -p /datasets/shakespeare && \
  hdfs dfs -put shakespeare/* /datasets/shakespeare/ 2>/dev/null || true
"

# ─── Apache Access Log ────────────────────────────────────────────────────────
echo "📂 Loading Apache access log..."
docker exec namenode bash -c "
  hdfs dfs -mkdir -p /datasets/weblog && \
  hdfs dfs -put /home/training/training_materials/developer/data/access_log.gz \
           /datasets/weblog/ 2>/dev/null || true
"

# ─── Bible ────────────────────────────────────────────────────────────────────
echo "📂 Loading Bible corpus..."
docker exec namenode bash -c "
  cd /tmp && \
  tar -xzf /home/training/training_materials/developer/data/bible.tar.gz 2>/dev/null || true && \
  hdfs dfs -mkdir -p /datasets/bible && \
  hdfs dfs -put bible/* /datasets/bible/ 2>/dev/null || true
"

echo ""
echo "✅ All datasets loaded. Verify with:"
echo "   docker exec namenode hdfs dfs -ls /datasets"
