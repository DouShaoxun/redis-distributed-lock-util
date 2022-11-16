# 基于Redis实现分布式锁
## 引入依赖
```xml
<dependency>
    <groupId>cn.cruder.dousx</groupId>
    <artifactId>redis-distributed-lock-starter</artifactId>
    <version>lastVersion</version>
</dependency>
```
## 开启Rdl
`@EnableRdl`
```java
@EnableRdl
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```