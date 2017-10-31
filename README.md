[TOC]

# SpringWebSocketDemo

Spring WebSocket Demo.

## 1. 开发环境

JDK 1.8

Tomcat 8.0+

修改tomcat目录下的 conf/tomcat-users.xml。在`</tomcat-users>`上面添加角色和用户。

```xml
<role rolename="ws"/>
<user username="a" password="1" roles="ws"/>
<user username="b" password="2" roles="ws"/>
<user username="c" password="3" roles="ws"/>
```

## 2. spring-websocket

### 2.1 pom.xml

依赖`spring-websocket`、`spring-messaging`。

```xml
	<properties>
        <java.version>1.8</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>4.3.12.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-websocket</artifactId>
            <version>4.3.12.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-messaging</artifactId>
            <version>4.3.12.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.39</version>
        </dependency>
        <!--<dependency>-->
        <!--<groupId>com.fasterxml.jackson.core</groupId>-->
        <!--<artifactId>jackson-databind</artifactId>-->
        <!--<version>2.9.2</version>-->
        <!--</dependency>-->
    </dependencies>
```

### 2.2 web.xml

在`web.xml`中的`servlet`和`filter`中添加`<async-supported>true</async-supported>`。

**注意添加的位置！位置不对会报错的！**

```xml
    <!-- 配置DispatcherServlet -->
    <servlet>
        <servlet-name>SpringMVC</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>
                classpath:spring/spring-mvc.xml,
                classpath:spring/spring-websocket.xml
            </param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
        <async-supported>true</async-supported>
    </servlet>

    <servlet-mapping>
        <servlet-name>SpringMVC</servlet-name>
        <!-- 默认匹配所有的请求 -->
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <!-- spring框架提供的字符集过滤器 -->
    <!-- spring Web MVC框架提供了org.springframework.web.filter.CharacterEncodingFilter用于解决POST方式造成的中文乱码问题  -->
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <async-supported>true</async-supported>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <!-- force强制 -->
        <init-param>
            <param-name>forceEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

### 2.3 spring-websocket.xml

spring-websocket.xml一定要配置在`contextConfigLocation`里，这样扫描`@Controller`时才会扫到`@MessageMapping`。

```xml
	<bean class="com.alibaba.fastjson.support.spring.FastjsonSockJsMessageCodec"
            id="fastjsonSockJsMessageCodec"/>

    <!-- '/app'是客户端向服务端发送消息时使用的前缀，controller中的 @MessageMapping("hello"),send时的地址应该为'/app/hello' -->
    <!-- '/user'是服务端向客户端广播指定用户时所用的前缀 -->
    <websocket:message-broker application-destination-prefix="/app" user-destination-prefix="/user">
        <!-- allowed-origins 用以解决跨域问题，这里的 * 表示允许所有域名请求 -->
        <websocket:stomp-endpoint allowed-origins="*" path="/websocket"/>
        <websocket:stomp-endpoint allowed-origins="*" path="/sockjs">
            <!-- MessageCodec，必须要配置，否则报错 -->
            <websocket:sockjs
                    message-codec="fastjsonSockJsMessageCodec">
            </websocket:sockjs>
        </websocket:stomp-endpoint>
        <!--stomp-broker-relay 和 simple-broker 二者只能出现一个。-->
        <!-- <websocket:stomp-broker-relay prefix="/topic,/user"/> -->
        <!-- 客户端可以订阅的 destination 的前缀,用','分隔 -->
        <websocket:simple-broker prefix="/topic,/user"/>
    </websocket:message-broker>
```

### 2.4 @Controller

`@MessageMapping` 服务端接受客服端发过来的消息

```java
@MessageMapping("hello")
public void hello(String json) {
    System.out.println("Hello Word!" + json);
}
```



客户端通过send()方法向服务端发送消息。
client.send(destination, headers, body);

这个方法必须有一个参数，用来描述对应的STOMP的目的地。另外可以有两个可选的参数：`headers`，`object`类型包含额外的信息头部；`body`，一个String类型的参数。

如果你想发送一个有`body`的信息，也必须传递`headers`参数。如果没有headers需要传递，那么就传{}即可，如下所示：

client.send(destination, {}, body);

```javascript
function hello() {
    stompClient.send("/app/hello", {}, JSON.stringify({"id": "1", "name": "jack"}));
}
```
**注：STOMP消息的`body`必须为字符串。如果你需要发送/接收`JSON`对象，你可以使用`JSON.stringify()`和`JSON.parse()`去转换JSON对象。**



`@SendTo` 服务端向客户端发送消息

`@DestinationVariable` 获取destination中的值

```java
@MessageMapping("sendTo/{id}")
@SendTo("/topic/user")
public String sendTo(@DestinationVariable("id") String id) {
    User user = new User();
    user.setId(id);
    user.setName("Spring");
    user.setAge(20);
    return JSON.toJSONString(user);
}
```



客户端通过订阅destination来获取消息

client.subscribe(destination, callback,headers);

这个方法有2个必需的参数：目的地(`destination`)，回调函数(`callback`)；还有一个可选的参数`headers`。其中`destination`是String类型，对应目的地，回调函数是伴随着一个参数的`function`类型。

```javascript
// 订阅 '/topic/user'
stompClient.subscribe('/topic/user', function (frame) {
    var user = JSON.parse(frame.body);
    console.info(user);
});
```


`@SendToUser`发送给当前用户

```java
@MessageMapping("sendToUser/{id}")
@SendToUser(value = "/user", broadcast = false)
public String sendToUser(@DestinationVariable("id") String id, Principal principal) {
    User user = new User();
    user.setId(id);
    user.setName(principal.getName());
    user.setAge(24);
    return JSON.toJSONString(user);
}
```

```js
// 订阅 '/user/user'
stompClient.subscribe('/user/user', function (frame) {
    var user = JSON.parse(frame.body);
    console.info(user);
});
```



还可以通过`SimpMessagingTemplate`对象进行服务端向客户端发送消息

```java
@Autowired
private final SimpMessagingTemplate template;
```

```java
@MessageMapping("convertAndSend/{id}")
public void convertAndSend(@DestinationVariable("id") String id) {
    User user = new User();
    user.setId(id);
    user.setName("Spring");
    user.setAge(20);
    this.template.convertAndSend("/topic/user", JSON.toJSONString(user));
}

@MessageMapping("convertAndSendToUser/{id}")
public void convertAndSendToUser(@DestinationVariable("id") String id, Principal principal) {
    User user = new User();
    user.setId(id);
    user.setName(principal.getName());
    user.setAge(20);
    // 可以给多个指定用户推送消息
    this.template.convertAndSendToUser("a", "/user", JSON.toJSONString(user));
    this.template.convertAndSendToUser("b", "/user", JSON.toJSONString(user));
}
```



## 3. 注

@MessageMapping 不支持自动json转对象。

@SendTo 可以自动对象转json，需要依赖`jackson-databind` 。

Jrebel也不支持该框架，添加、修改注解必须要重启项目，修改方法内代码不需要。



参考资料：

[Stomp Over Websocket文档](https://segmentfault.com/a/1190000006617344)

[gs-messaging-stomp-websocket](http://spring.io/guides/gs/messaging-stomp-websocket/)

[WebSockets](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket)

