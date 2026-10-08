#!/bin/bash
set -e

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
# Render 注入 PORT；同时给 Spring Boot 标准变量
export SERVER_PORT="${PORT:-8080}"

if [ -n "${DATABASE_URL:-}" ] || [ -n "${DATABASE_USERNAME:-}" ] || [ -n "${DATABASE_PASSWORD:-}" ]; then
  : "${DATABASE_URL:?Set DATABASE_URL for the external database}"
  : "${DATABASE_USERNAME:?Set DATABASE_USERNAME for the external database}"
  : "${DATABASE_PASSWORD:?Set DATABASE_PASSWORD for the external database}"
else
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-devflow123}"
export DATABASE_USERNAME=root
export DATABASE_PASSWORD="$MYSQL_ROOT_PASSWORD"
export DATABASE_URL="jdbc:mysql://127.0.0.1:3306/devflow?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"

BOOTSTRAP_MARKER=/var/lib/mysql/.devflow_bootstrapped
NEED_BOOTSTRAP=0
if [ ! -f "$BOOTSTRAP_MARKER" ]; then
  # apt 安装会留下 auth_socket 的 root；清空后 initialize-insecure，便于密码登录
  rm -rf /var/lib/mysql/*
  mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
  NEED_BOOTSTRAP=1
fi

# Render Free 约 512MB：MySQL + JVM 必须压内存，否则会被 OOM kill (exit 137)
mysqld --user=mysql --datadir=/var/lib/mysql \
  --bind-address=127.0.0.1 \
  --port=3306 \
  --performance-schema=OFF \
  --skip-log-bin \
  --innodb-buffer-pool-size=24M \
  --innodb-log-buffer-size=1M \
  --max-connections=20 \
  --table-open-cache=32 \
  --thread-cache-size=2 \
  --key-buffer-size=4M \
  --tmp-table-size=4M \
  --max-heap-table-size=4M \
  --sort-buffer-size=256K \
  --read-buffer-size=256K \
  --join-buffer-size=256K \
  --net-buffer-length=4K \
  &

for i in $(seq 1 90); do
  if mysqladmin --protocol=socket -uroot ping --silent 2>/dev/null \
    || mysqladmin -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" ping --silent 2>/dev/null \
    || mysqladmin -h 127.0.0.1 -uroot ping --silent 2>/dev/null; then
    break
  fi
  sleep 1
done

if [ "$NEED_BOOTSTRAP" = "1" ]; then
  mysql --protocol=socket -uroot -e "
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
CREATE DATABASE IF NOT EXISTS devflow DEFAULT CHARSET utf8mb4;
FLUSH PRIVILEGES;
"
  touch "$BOOTSTRAP_MARKER"
fi
fi

# SerialGC 峰值更低，适合 512MB 小实例
exec java ${JAVA_OPTS:--Xmx160m -Xms48m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m -XX:ReservedCodeCacheSize=32m -XX:+TieredCompilation -XX:TieredStopAtLevel=1} -jar /app/app.jar
