JPA使用总结

1. 使用@Entity需要有无参数的构造函数
2. 使用jpa可以不用首先新建表结构
3. 在MySQL中，Java代码中的enum会被转化为int
4. 不要指望CrudRepository可以将String转换为Type
5. 在Order.java中使用@ManyToMany(targetEntity = Taco.class)，会**自动创建一张表**，表明为@Table_Name+"_"+@filedname；并在后续的处理中新增数据。
6. 使用@GeneratedValue(strategy = GenerationType.AUTO)，在MySQL环境下，会创建一个hibernate_sequence的表，建议改为**GenerationType.IDENTITY**

