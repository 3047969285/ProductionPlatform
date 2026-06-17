#!/bin/bash
set -e

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-devflow123}"
export DATABASE_USERNAME=root
export DATABASE_PASSWORD="$MYSQL_ROOT_PASSWORD"
export DATABASE_URL="jdbc:mysql://127.0.0.1:3306/devflow?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
export SPRING_PROFILES_ACTIVE=prod
export SQL_INIT_MODE=always

# 初始化 MySQL 数据目录
if [ ! -d /var/lib/mysql/mysql ]; then
  mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
fi

mysqld --user=mysql --datadir=/var/lib/mysql \
  --innodb-buffer-pool-size=64M \
  --max-connections=40 \
  --bind-address=127.0.0.1 &

for i in $(seq 1 60); do
  if mysqladmin ping -h 127.0.0.1 -uroot --silent 2>/dev/null; then
    break
  fi
  sleep 1
done

mysql -h 127.0.0.1 -uroot -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';" 2>/dev/null || true
mysql -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS devflow DEFAULT CHARSET utf8mb4;" 2>/dev/null \
  || mysql -h 127.0.0.1 -uroot -e "CREATE DATABASE IF NOT EXISTS devflow DEFAULT CHARSET utf8mb4;"

exec java ${JAVA_OPTS:--Xmx384m -Xms256m} -jar /app/app.jar
