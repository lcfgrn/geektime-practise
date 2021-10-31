# Redission介绍

Redission帮我们实现好了分布式的锁，可以直接拿来用

用Redission在两个JVM连接同一个Redis时，创建的Map实际上是同一个Map，实现了分布式Map

# 内存网格-Hazelcast介绍

特性：

1. 分布式的
2. 高可用：集群的每个节点都是active模式，可以提供业务查询和数据修改事务；
3. 可扩展的
4. 面向对象的
5. 低延迟：基于内存的，可以使用堆外内存 vert.x

## 部署模式

Client-Server模式

嵌入（Embedded）模式

内存网格——Hazelcast支持事务

