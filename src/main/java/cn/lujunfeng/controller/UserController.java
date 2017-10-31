package cn.lujunfeng.controller;

import cn.lujunfeng.entity.User;
import com.alibaba.fastjson.JSON;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {

    private final SimpMessagingTemplate template;

    @Autowired
    public UserController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("hello")
    public void hello(String json) {
        System.out.println("Hello Word!" + json);
    }

    @MessageMapping("sendTo/{id}")
    @SendTo("/topic/user")
    public String sendTo(@DestinationVariable("id") String id) {
        User user = new User();
        user.setId(id);
        user.setName("Spring");
        user.setAge(20);
        return JSON.toJSONString(user);
    }


    @MessageMapping("sendToUser/{id}")
    @SendToUser(value = "/user", broadcast = false)
    public String sendToUser(@DestinationVariable("id") String id, Principal principal) {
        User user = new User();
        user.setId(id);
        user.setName(principal.getName());
        user.setAge(24);
        return JSON.toJSONString(user);
    }

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

}
