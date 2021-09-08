# Java8学习笔记

## Java Lambda表达式

Lambda表达式实际上就是一个匿名函数。

### 表达形式

当使用Lambda表达式时，如果只有一行，可以省略大括号

```java
(parameters) -> expression
```

或

```java
(parameters) -> {statements;}
```

### 几种Lambda表达式

1. 不需要参数，有返回值

   ```java
   () -> 5
   ```

2. 有1个参数，有返回值

   ```java
   x -> 2*x
   ```

3. 有2个参数，有返回值

   ```java
   (x,y) -> x+y
   ```

4. 有参数，无返回值

   ```java
   (String s) -> System.out::println
   ```

### 深入Java8函数式 

#### @FunctionalInterface函数式接口

- Predicate<T> 有参数、条件判断
- Function<T, R> 有参数、有返回值
- Consumer<T> 无返回值
- Supplier<T> 无参数、有返回值  

------

## Stream流

- 定义：是一个来自数据源的元素队列并支持聚合操作
- 元素：特定类型的对象，形成一个队列。Stream不会存储元素，而是按需计算
- 数据源：流的来源。集合、数组，I/O channel，产生器generator等
- 聚合操作：filter，map，reduce，find，match，sorted等

### 两个基础特征

- Pipelining：中间操作都会返回流对象本身。多个操作可以串联成一个管道
- 内部迭代：通过访问者模式（Visitor）实现

### Stream操作

#### 中间操作

##### 1.选择与过滤

- filter(Predicate p)
- distinct()
- limit(long maxSize)
- skip(long n)：跳过前n个元素的流，如流中元素不足n个，则返回一个空流

##### 2.映射

- map(Function f)：将该函数应用到每个元素上，将其映射成一个新的元素
- mapToDouble(ToDoubleFunction f)
- mapToInt(ToIntFunction f)
- mapToLong(ToLongFunction f)
- flatMap(Function f)：将流中每个值都换成另一个流，然后把所有流连接成一个流

##### 3.排序

- sorted()

- sorted(Comparator comp)

#### 终止操作

##### 1.查找与匹配

- allMatch
- anyMatch
- noneMatch
- findFirst
- findAny
- max
- min

##### 2.归约

- reduce：需要初始值

##### 3.收集

- collect：
  - toList
  - toSet
  - toCollection
- count 计算流中元素的个数
- summaryStatistics 统计最大最小平均值

##### 4.迭代

- forEach