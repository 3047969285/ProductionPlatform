#!/bin/bash
set -e

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-devflow123}"
export DATABASE_USERNAME=root
export DATABASE_PASSWORD="$MYSQL_ROOT_PASSWORD"
export DATABASE_URL="jdbc:mysql://127.0.0.1:3306/devflow?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
export SPRING_PROFILES_ACTIVE=prod
export SQL_INIT_MODE=always

BOOTSTRAP_MARKER=/var/lib/mysql/.devflow_bootstrapped
NEED_BOOTSTRAP=0
if [ ! -f "$BOOTSTRAP_MARKER" ]; then
  # apt 安装会留下 auth_socket 的 root；清空后 initialize-insecure，便于密码登录
  rm -rf /var/lib/mysql/*
  mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
  NEED_BOOTSTRAP=1
fi

mysqld --user=mysql --datadir=/var/lib/mysql \
  --innodb-buffer-pool-size=64M \
  --max-connections=40 \
  --bind-address=127.0.0.1 &

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

exec java ${JAVA_OPTS:--Xmx384m -Xms256m} -jar /app/app.jar
