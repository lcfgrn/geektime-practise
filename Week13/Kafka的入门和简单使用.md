# Kafka的入门和简单使用

Kafka是一种分布式的，基于发布/订阅的消息系统

## 设计目标

1. 以时间复杂度为O(1)的方式提供**持久化**能力，即使对TB级别的数据也能保证常熟时间复杂度的访问性能
2. **高吞吐率**，单机支持每秒100k条以上消息的传输
3. 支持Kafka Server间的消息分区，及分布式消费，同时保证每个Partition内的消息**顺序**传输
4. 支持离线数据处理和实时数据处理
5. Scale out：支持在线**水平扩展**

## 基本概念

1. Broker：kafka集群包含一个或多个服务器，这种服务器称为broker
2. Topic：与通用的mq中的topic概念一致，每个Topic可能包含一个或多个Partition
3. **Partition**：物理上的概念
4. Producer：负责发布消息到Kafka broker
5. Consumer：向Kafka broker读取消息的客户端
6. **Consumer Group**：每个Consumer都属于一个特定的Consumer Group，把整个Consumer Group看做是一个消费者，按消费者组为单位拿到队列的所有消息

## 单机部署

## 集群部署

需要额外配置一个Zookeeper

最新发布的Kafka版本，zookeeper被去掉，自行管理数据

## Topic和Partition

多Partition支持水平扩展和并行处理，顺序写入提升吞吐性能

每个partition可以通过**副本因子**添加多个副本

### Topic特性

1. 通过partition增加可扩展性
2. 通过顺序写入达到高吞吐
3. 多副本增加容错性（实践中一般用：3副本+2确认 1宕机 5副本+3确认 2宕机）

## 单机安装部署

1. kafka安装
   - http://kafka.apache.org/downloads
2. 启动kafka
   - 修改配置
     - vim config/server.properties
     - 打开listeners=PLAINTEXT://localhost:9092
   - 启动服务
     - bin/zookeeper-server-start.sh config/zookeeper.properties
     - bin/kafka-server-start.sh config/server.properties		

## 单机部署测试

由于版本是最新版本，使用的是kafka自带的bootstrap-server，而非zookeeper，命令会有所差异

- 启动服务

```shell
cd kafka_2.13-3.0.0/
bin/kafka-server-start.sh -daemon config/server.properties
```

- 添加主题

```shell
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic testk --partition 3 --replication-factor 1
```

- 查看主题

```shell
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic testk
```

- 创建生产者

```shell
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic testk
```

- 创建消费者

```shell
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic testk
```

## 简单性能测试

- 生产者性能测试

```shell
bin/kafka-producer-perf-test.sh --topic testk --num-records 100000 --record-size 1000 --throughput 2000 --producer-props bootstrap.servers=localhost:9092
```

- 消费者性能测试

```shell
bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9092 --topic testk --fetch-size 1048576 --messages 100000 --threads 1
```

基于Kafka Client发送和接收消息——极简生产者

