package br.com.julio.pi.backoffice_users.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class WhoAmIController {
    @GetMapping("/api/whoami")
    public Map<String,Object> who(HttpServletRequest req, Authentication a) {
        return Map.of(
            "principal", a==null ? null : a.getPrincipal(),
            "authorities", a==null ? null : a.getAuthorities(),
            "CLIENT_ID", req.getAttribute("CLIENT_ID")
        );
    }
}