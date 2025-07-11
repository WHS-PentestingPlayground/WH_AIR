# #!/bin/bash
# iptables -P OUTPUT DROP
# iptables -A OUTPUT -o lo -j ACCEPT

# # web-server(172.31.0.2), admin-server(172.30.0.4)로의 아웃바운드 허용
# iptables -A OUTPUT -d 172.31.0.2 -j ACCEPT
# iptables -A OUTPUT -d 172.30.0.4 -j ACCEPT

# exec su postgres -c "docker-entrypoint.sh $@" 