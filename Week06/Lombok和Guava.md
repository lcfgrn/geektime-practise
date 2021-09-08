# Lombok和Guava

## Lombok是什么？

基于字节码增强，编译期处理  

## 几种常用的Lombok注解

- @Data

相当于 @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode的集合

- @Setter @Getter
- XXXConstructor
- Builder
- ToString
- Slf4j
- Log

## 什么是是Guava

Guava 是一种基于开源的 Java 库，其中包含谷歌正在由他们很多项目使用的很多核心库  

### 几种核心库

#### 集合

1. 不可变集合

2. 新集合类型

   multisets, multimaps, tables, bidirectional maps  

3. 强大的集合工具类

4. 扩展工具类

#### 缓存

本地缓存实现

```java
LoadingCache<Key, Graph> graphs = CacheBuilder.newBuilder()
		.maximumSize(1000)
		.expireAfterWrite(10, TimeUnit.MINUTES)
		.removalListener(MY_LISTENER)
		.build(
			new CahceLoader<Key, Graph>() {
				public Graph load(Key key) throws AnyException {
                    return createExpensiveGraph(key);
                }
			});
```

#### 并发

代码实现