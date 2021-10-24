# Redis详解

## Redis基本功能

### Redis安装

### Redis的5种基础数据结构

#### 字符串

字符串类型的value最多可以容纳的数据长度为512M

set/get/del/getall/exists/append

incr/decr/incrby/decrby

注意：

1. 字符串append：会使用更多的内存
2. 整数共享
3. 整数精度问题：redis大概能保证16位的精度

#### 散列（hash）——map-pojo

可以看成key-value的map容器

hset/hget/hmset/hmget/hgetall/hdel/hincrby

hexists/hlen/hkeys/hvals

```sql
hset h1 a 1 b 2 c 3
```

#### 列表（list）——java中的LinkedList

lpush/rpush/lrange/lpop/rpop

#### 集合（set）——java的set，不重复的list

sadd/srem/smembers/sismember~set.add,remove,contains,

sdiff/sinter/sunion~

#### 有序集合（sorted set)

每个成员都会有一个分数与之关联

zadd/zrange/zscore

### Redis的3种高级数据结构

#### Bitmaps

setbit/getbit/bitop/bitcount/bitpos

#### Hyperloglogs

pfadd/pfcount/pfmerge

#### GEO

geoadd/geohash/geopos/geodist/georadius/georadiusbymember

## Redis到底是单线程，还是多线程

redis6之前，BIO，单线程

**redis内存里处理数据都是单线程**

网络IO接入，采用NIO模型

## Redis的6大使用场景

### 经典用法：业务数据缓存

1. 通用数据缓存，string，int，list，map等
2. 实时热数据，最新500条数据
3. 会话缓存，token缓存等

### 业务数据处理

1. 非严格一致性要求的数据：评论、点击等
2. 业务数据去重：订单处理的幂等校验等
3. 业务数据排序

### 全局一致性计数

1. 全局流控计数
2. 秒杀的库存计算
3. 抢红包
4. 全局ID生成

### 高效统计计数

1. id去重，记录访问IP等全局bitmap操作
2. UV、PV等访问量==》非严格一致性要求

### 发布订阅与Stream

1. Pub-Sub模拟队列
2. Redis Stream

### 分布式锁

1.获取锁——单个原子性操作

`set dlock my_random_value NX PX 30000`

2.释放锁——lua脚本-保证原子性+单线程，从而具有事务性

```lua
if redis.call("get",KEYS[1]) == ARGV[1] then
    return redis.call("del", KEY[1])
else
    return 0
end
```

## Redis的Java客户端

#### Jedis

类似于JDBC，可以看做对redis命令的包装

基于BIO，线程不安全，需要配置连接池管理连接

#### Lettuce

目前主流推荐的驱动，基于Netty NIO，API线程安全，Spring Boot默认的驱动

#### Redission

基于Netty NIO，API线程安全

亮点：大量丰富的分布式功能特性，比如JUC的线程安全集合和工具的分布式版本，分布式的基本数据类型和锁等

## Redis与Spring的整合

#### Spring Data Redis

核心是RedisTemplate(可以配置基于Jedis，Lettuce， Redisson)

使用方式类似于MongoDBTemplate，JDBCTemplate或JPA

#### Spring Boot

引入**spring-boot-starter-data-redis**

#### Spring Cache与Redis集成

默认使用全局的CacheManager自动集成

使用ConcurrentHashMap或ehcache，不需要考虑序列化问题

redis的话，需要

1. 需要使用java的对象序列化



