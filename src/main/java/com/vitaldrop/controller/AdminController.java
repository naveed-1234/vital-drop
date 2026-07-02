package com.vitaldrop.controller;

import com.vitaldrop.dto.AdminStats;
import com.vitaldrop.entity.Admin;
import com.vitaldrop.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Admin login(
            @RequestParam String username,
            @RequestParam String password) {

        return adminService.login(username, password);
    }

    @GetMapping("/stats")
    public AdminStats getStats() {

        return adminService.getDashboardStats();

    }
}