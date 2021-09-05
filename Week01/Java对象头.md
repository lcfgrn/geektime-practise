# Java对象头

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.8</version>
</dependency>
```


```java
class B{
    
}

public class ObjectHead {
    static B b = new B();

    public static void main(String[] args) {
        //jvm信息
        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseInstance(b).toPrintable());
    }
}
```


```
# Running 64-bit HotSpot VM.
# Using compressed oop with 3-bit shift.
# Using compressed klass with 3-bit shift.
# Objects are 8 bytes aligned.
# Field sizes by type: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
# Array element sizes: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]

com.mine.test.B object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

```

分析结果：整个对象16B，其中对象头12B，还有4B是对齐的字节，由于对象中没有任何字段，所以该对象的示例数据为0B

### 什么是对象的实例数据


```java
public class B {
    boolean aBoolean;
}
```


```
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total
```

整个对象的大小还是16B，其中对象头12B，boolean字段aBoolean占1B，剩下3B是对齐字节。**我们可以认为一个对象的布局大体分为三个部分分别是：对象头（Object head），对象的实例数据和对齐字节。**

### 对象头12B存储什么数据

openJDK文档中的概念

#### object header

Common structure at the beginning of every GC-managed heap object. (Every oop points to an object header.) Includes fundamental information about the heap object's layout, type, GC state, synchronization state, and identity hash code. Consists of two words. In arrays it is immediately followed by a length field. Note that both Java objects and VM-internal objects have a common object header format.

每个gc托管堆对象开头的公共结构。(每个oop都指向一个对象头。)包括关于堆对象的布局、类型、GC状态、同步状态和标识哈希码的基本信息。由两个词组成。在数组中，紧随其后的是长度字段。注意，Java对象和vm内部对象都有共同的对象头格式。

两个词

**klass pointer**

The second word of every object header. Points to another object (a metaobject) which describes the layout and behavior of the original object. For Java objects, the "klass" contains a C++ style "vtable".

每个对象头文件的第二个字。指向另一个对象(元对象)，它描述了原始对象的布局和行为。对于Java对象，“klass”包含一个c++风格的“vtable”。

**mark word**

The first word of every object header. Usually a set of bitfields including synchronization state and identity hash code. May also be a pointer (with characteristic low bit encoding) to synchronization related information. During GC, may contain GC state bits.

每个对象头文件的第一个字。通常是一组位域，包括同步状态和标识哈希码。也可能是同步相关信息的指针(具有特征的低比特编码)。在GC期间，可能包含GC状态位。

由openJDK文档可知，Java对象头由mark word和klass pointer两部分组成。

mark word中存储着对象的hashcode，分代年龄、锁标记位等信息。

klass pointer即类型指针，是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。

一个对象头的大小是12B，其中8B是mark word，剩下4B就是klass pointer了。


```
64 bits:
//  --------
//  unused:25 hash:31 -->| unused:1   age:4    biased_lock:1 lock:2 (normal object)
//  JavaThread*:54 epoch:2 unused:1   age:4    biased_lock:1 lock:2 (biased object)
//  PromotedObject*:61 --------------------->| promo_bits:3 ----->| (CMS promoted object)
//  size:64 ----------------------------------------------------->| (CMS free block)
//
//  unused:25 hash:31 -->| cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && normal object)
//  JavaThread*:54 epoch:2 cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && biased object)
//  narrowOop:32 unused:24 cms_free:1 unused:4 promo_bits:3 ----->| (COOPs && CMS promoted object)
//  unused:21 size:35 -->| cms_free:1 unused:7 ------------------>| (COOPs && CMS free block)

```

在无锁的情况下mark word中的分布情况


```java
public static void countHash(Object object) throws NoSuchFieldException, IllegalAccessException {
    // 手动计算HashCode
    Field field = Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    Unsafe unsafe = (Unsafe) field.get(null);
    long hashCode = 0;
    for (long index = 7; index > 0; index--) {
        // 取Mark Word中的每一个Byte进行计算
        hashCode |= (unsafe.getByte(object, index) & 0xFF) << ((index - 1) * 8);
    }
    String code = Long.toHexString(hashCode);
    System.out.println("util-----------0x"+code);
}
```


```java
 B b = new B();
System.out.println("before hash");
//没有计算HASHCODE之前的对象头
System.out.println(ClassLayout.parseInstance(b).toPrintable());
//JVM 计算的hashcode
System.out.println("jvm------------0x"+Integer.toHexString(b.hashCode()));
HashUtil.countHash(b);
//当计算完hashcode之后，我们可以查看对象头的信息变化
System.out.println("after hash");
System.out.println(ClassLayout.parseInstance(b).toPrintable());
```


```
before hash
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total

jvm------------0x2530c12
util-----------0x2530c12
after hash
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           01 12 0c 53 (00000001 00010010 00001100 01010011) (1393299969)
      4     4           (object header)                           02 00 00 00 (00000010 00000000 00000000 00000000) (2)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total
```

分析结果：
上面没有进行hashcode之前的对象头信息，可以看到的56bit没有值，打印之后hashcode就有值了

其中两行是通过hashcode方法打印的结果，第一行是根据1-7B的信息计算出来的hashcode，所以可以确定Java对象头中的mark word里面后7个字节存储的是hashcode信息。

那么第一个字节当中的8位分别存的是**分代年龄、偏向锁信息和对象状态**，这8bit分别如下图，这个图也会随对象状态改变而改变，下面是无锁状态下

![image](C:/Users/Lenovo/Pictures/mark_word_1.png)

关于对象状态一共有5种状态，分别是无锁、偏向锁、轻量锁、重量锁和GC标记。

2bit如果能存储5种状态？虚拟机把偏向锁和无锁表示为一种状态，然后根据图中偏向锁的标识再去标识无所还是偏向锁状态

#### 偏向级锁的对象头

```java
//-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0
B b = new B();
out.println("befor lock");
out.println(ClassLayout.parseInstance(b).toPrintable());
synchronized (b){
    out.println("lock ing");
    out.println(ClassLayout.parseInstance(b).toPrintable());
}
out.println("after lock");
out.println(ClassLayout.parseInstance(b).toPrintable());
```


```
befor lock
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total

lock ing
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           18 f6 d0 02 (00011000 11110110 11010000 00000010) (47248920)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total

after lock
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total
```

上面的程序调用一个synchronized代码块，故而应该是偏向锁，但却是轻量级锁，而且最后输出的结果依然和未加锁之前是一样的，这是因为虚拟机在启动时对偏向锁有延迟，sleep 5秒结果就会不一样了。


```
...
//-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0
Thread.sleep(5000);
B b = new B();
...
    
```


```
befor lock
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total

lock ing
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           05 38 90 02 (00000101 00111000 10010000 00000010) (43005957)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total

after lock
com.mine.test.B object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0     4           (object header)                           05 38 90 02 (00000101 00111000 10010000 00000010) (43005957)
      4     4           (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4           (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     1   boolean B.aBoolean                                false
     13     3           (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 3 bytes external = 3 bytes total
```

结果变成了00000101

为什么偏向锁会延迟？因为启动程序时，虚拟机会有很多操作，包括gc等等。虚拟机刚运行时会有大量同步方法，很多都不是偏向锁，**而偏向锁升级为轻/重量级锁很费时间和资源，因此虚拟机会延迟4s左右再开启偏向锁。**

#### 轻量级锁的对象头


```java
before lock
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           82 c1 00 f8 (10000010 11000001 00000000 11111000) (-134168190)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

lock ing
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           f0 f3 39 03 (11110000 11110011 00111001 00000011) (54129648)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           82 c1 00 f8 (10000010 11000001 00000000 11111000) (-134168190)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

after lock
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           82 c1 00 f8 (10000010 11000001 00000000 11111000) (-134168190)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
```

#### 重量级的对象头


```java
a = new A();
System.out.println("befre lock");
System.out.println(ClassLayout.parseInstance(a).toPrintable());//无锁

Thread t1= new Thread(){
    public void run() {
        synchronized (a){
            try {
                Thread.sleep(5000);
                System.out.println("t1 release");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
};
t1.start();
Thread.sleep(1000);
System.out.println("t1 lock ing");
System.out.println(ClassLayout.parseInstance(a).toPrintable());//轻量锁
sync();
System.out.println("after lock");
System.out.println(ClassLayout.parseInstance(a).toPrintable());//重量锁
System.gc();
System.out.println("after gc()");
System.out.println(ClassLayout.parseInstance(a).toPrintable());//无锁---gc
```


```java
public  static  void sync() throws InterruptedException {
    synchronized (a){
        System.out.println("t1 main lock");
        out.println(ClassLayout.parseInstance(a).toPrintable());//重量锁
    }
}
```


```
befre lock
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           05 c2 00 f8 (00000101 11000010 00000000 11111000) (-134168059)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

t1 lock ing
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           28 f4 52 20 (00101000 11110100 01010010 00100000) (542307368)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           05 c2 00 f8 (00000101 11000010 00000000 11111000) (-134168059)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

t1 release
t1 main lock
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           4a 11 89 1c (01001010 00010001 10001001 00011100) (478744906)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           05 c2 00 f8 (00000101 11000010 00000000 11111000) (-134168059)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

after lock
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           4a 11 89 1c (01001010 00010001 10001001 00011100) (478744906)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           05 c2 00 f8 (00000101 11000010 00000000 11111000) (-134168059)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

after gc()
com.mine.test.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           09 00 00 00 (00001001 00000000 00000000 00000000) (9)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           05 c2 00 f8 (00000101 11000010 00000000 11111000) (-134168059)
     12     4    int A.i                                       0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
```

上述代码总结：

lock：锁状态标记位，值不同，整个mark word表示的含义不同。

biased_lock：偏向锁标记（1启用偏向锁，0不启用）

| biased_lock | lock | 状态     |
| ----------- | ---- | -------- |
| 0           | 01   | 无锁     |
| 1           | 01   | 偏向锁   |
| 0           | 00   | 轻量级锁 |
| 0           | 10   | 重量级锁 |
| 0           | 11   | GC标记   |

#### 性能对比轻量级锁和偏向锁


```java
public class A {
    int i=0;
   
    public synchronized void parse(){
        i++;
        
    }
    //JOLExample6.countDownLatch.countDown();
}
```

```
A a = new A();
long start = System.currentTimeMillis();
//调用同步方法1000000000L 来计算1000000000L的++，对比偏向锁和轻量级锁的性能
//如果不出意外，结果灰常明显
for(int i=0;i<1000000000L;i++){
    a.parse();
}
long end = System.currentTimeMillis();
System.out.println(String.format("%sms", end - start));
```

轻量级锁的执行结果


```
19993ms
```

让偏向锁启动无延时

-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0

结果：


```
1774ms
```

**需要注意的是如果对象已经计算了hashcode就不能偏向了**

**小端存储**

即最低地址存放的最低字节，一个用十六进制表示的32位数据：12345678H，存放在存储字长是32位的存储单元中，按低字节到高字节的存储顺序为0x78、0x56、0x34和0x12。整个存储字从低字节到高字节读出的结果就是：78563412H，为Intel x86 系列等采用。

**整个mark word的信息分布**

![image](./mark_word_2.png)



