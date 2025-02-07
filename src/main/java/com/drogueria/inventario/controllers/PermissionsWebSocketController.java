package com.drogueria.inventario.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PermissionsWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastPermissionsUpdate(String username) {
        messagingTemplate.convertAndSend(
            "/topic/permissions-update", 
            username
        );
    }
}