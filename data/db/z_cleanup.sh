#!/bin/sh
# 모든 초기화가 끝난 뒤, initdb.d 내 SQL/SH 파일 삭제
rm -f /docker-entrypoint-initdb.d/*.sql /docker-entrypoint-initdb.d/*.sh 