# Redis集群

## 配置文件修改

- port

- pidfile
- dir
- io-threads
- appendonly
- replicaof ::1 6380（启动的时候指定本redis节点为从节点）

## Redis Sentinel主从切换

### 两种启动方式

- reids-sentinel sentinel.conf
- redis-server redis.conf --sentinel

## Redis Cluster：走向分片

- cluster-enabled yes

**注意：**

1. 节点间使用gossip通信，规模<1000
2. 默认所有槽位可用，才提供服务
3. 一般会配合主从模式使用

