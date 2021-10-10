## @SpringBootApplication

@SpringBootApplication是一个组合注解，它组合了3个其他的注解。

- @SpringBootConfiguration：将该类声明为配置类。这个注解实际上是@Configuration注解的特殊形式。
- @EnableAutoConfiguration：启用Spring Boot的自动配置。
- @ComponentScan：启用组件扫描。这样我们能够通过像@Component、@Controller、@Service这样的注解声明其他类，Spring会自动发现它们并将它们注册为Spring应用上下文中的组件。

## @SpringBootTest

@SpringBootTest会告诉JUnit在启动测试的时候要添加上Spring Boot的功能。从现在开始，我们可以将这个测试类视同为在main()方法中调用SpringApplication.run()

SpringBoot2.5.4中无需添加在测试类中添加@RunWith

## @RequestMapping及相关

| 注解            | 描述                |
| --------------- | ------------------- |
| @RequestMapping | 通用的请求处理      |
| @GetMapping     | 处理HTTP GET请求    |
| @PostMapping    | 处理HTTP POST请求   |
| @PutMapping     | 处理HTTP PUT请求    |
| @DeleteMapping  | 处理HTTP DELETE请求 |
| @PatchMapping   | 处理HTTP PATCH请求  |

## 支持的模板方案

| 模板                   | Spring Boot  starter依赖              | 缓存配置                     |
| ---------------------- | ------------------------------------- | ---------------------------- |
| Freemaker              | spring-boot-starter-freemaker         | spring.freemaker.cache       |
| Croovy Templates       | spring-booot-starter-groovy-templates | spring.groovy.template.cache |
| Java Server Pages(JSP) | 无(由Tomcat或Jetty提供)               |                              |
| Mustache               | spring-boot-starter-mustache          | spring.mustache.cache        |
| Thymeleaf              | spring-boot-starter-thymeleaf         | spring.thymeleaf.cache       |

## 构造型注解

Spring的组件扫描会自动发现它，并且会将其初始化为Spring应用上下文中的bean

### @Repository

### @Controller

### @Component

## @SessionAttributes

类级别的@SessionAttributes能够制定模型对象包保存在session中，这样才能跨请求使用

## @ModelAttribute(name="order")

确保会在模型中创建一个Order对象

