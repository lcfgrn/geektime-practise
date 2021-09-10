# Java并发编程实战——极客时间（课后题）

## 09 问题

下面代码的本意是当前线程被中断之后，退出while(true)，你觉得这段代码是否正确呢？

```java
Thread th = Thread.currentThread();
while(true) {
  if(th.isInterrupted()) {
    break;
  }
  // 省略业务代码无数
  try {
    Thread.sleep(100);
  }catch (InterruptedException e){
    e.printStackTrace();
  }
}
```

### 解答：

可能会出现无限循环，如果线程在sleep期间被打断了，会抛出一个InterruptedException异常，这时候这个异常会被catch到，导致无法推出循环，故应该在catch代码块中重置一下中断标识

```java
Thread th = Thread.currentThread();
while(true) {
  if(th.isInterrupted()) {
    break;
  }
  // 省略业务代码无数
  try {
    Thread.sleep(100);
  }catch (InterruptedException e){
  	Thread.currentThread().interrupt();
    e.printStackTrace();
  }
}
```

## 10 问题

有些同学对于最佳线程数的设置积累了一些经验值，认为对于 I/O 密集型应用，最佳线程数应该为：2 * CPU 的核数 + 1，你觉得这个经验值合理吗？

### 解答：

个人认为这个经验值有一定的合理性，如果是程序首次运行，可以将线程数设置为这个经验值，以得到比较好的运行效果；不过运行一段时间后，能够根据工具（如apm等）分析得出I/O耗时/CPU耗时的比值，则应该采用更优化的线程数设置（最佳线程数 =CPU 核数 * [ 1 +（I/O 耗时 / CPU 耗时）]）。

## 11 问题

常听人说，递归调用太深，可能导致栈溢出。你思考一下原因是什么？有哪些解决方案呢？

### 解答：

栈溢出原因：

每调用一个方法就会在线程栈上创建一个栈帧，如果是递归调用，由于是递归，导致一个方法没执行完，就去调用另一个方法，会导致在线程栈上创建很多的栈帧；如果创建的栈帧超过了线程栈的大小，则会导致栈溢出。

解决方法：

1. 使用循环替代递归方法。缺点：代码逻辑不够清晰
2. 限制递归次数
3. 使用尾递归，尾递归是指在方法返回时只调用自己本身，且不能包含表达式。编译器或解释器会把尾递归做优化，使递归方法不论调用多少次，都只占用一个栈帧，所以不会出现栈溢出。然鹅，Java没有尾递归优化。

## 12 问题

本期示例代码中，类 SafeWM 不满足库存下限要小于库存上限这个约束条件，那你来试试修改一下，让它能够在并发条件下满足库存下限要小于库存上限这个约束条件。

### 解答：


1. setUpper() 跟 setLower() 都加上 "synchronized" 关键字。不要太在意性能，老师都说了，避免过早优化。
2. 如果性能有问题，可以把 lower 跟 upper 两个变量封装到一个类中，例如
```java
@Builder
public class Boundary {
    private final long lower;
    private final long upper;
    
    public Boundary(long lower, long upper) {
        if(lower >= upper) {
            // throw exception
        }
        this.lower = lower;
        this.upper = upper;
    }
}
```
移除 SafeVM 的 setUpper() 跟 setLower() 方法，并增入 setBoundary(Boundary boundary) 方法。

