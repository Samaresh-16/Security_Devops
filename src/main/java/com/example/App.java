package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

@RestController
@RequestMapping("/")
class HelloController {

    // 🛑 VULNERABILITY: SQL Injection (Detected by SonarQube)
    @GetMapping("/user")
    public String getUser(@RequestParam String id) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "password123"); // 🚨 Hardcoded credentials
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM users WHERE id = '" + id + "'"); // 🚨 SQL Injection

            if (rs.next()) {
                return "User: " + rs.getString("name");
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage(); // 🚨 Bad exception handling
        }
    }

    // 🛑 VULNERABILITY: Cross-Site Scripting (XSS)
    @GetMapping("/hello")
    public String sayHello(@RequestParam String name) {
        return "Hello, " + name; // 🚨 No HTML escaping, XSS risk
    }

    // 🛑 VULNERABILITY: Sensitive Data Exposure
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        System.out.println("Logging in with credentials: " + username + " / " + password); // 🚨 Logs sensitive data
        return "Login successful!";
    }

    // 🛑 VULNERABILITY: Insecure Direct Object Reference (IDOR)
    @GetMapping("/admin-data")
    public Map<String, String> getAdminData() {
        Map<String, String> data = new HashMap<>();
        data.put("secretKey", "12345-ABCDE"); // 🚨 Hardcoded secret (SonarQube & Trivy)
        data.put("config", "Server running in debug mode");
        return data;
    }

    // 🛑 VULNERABILITY: Insecure File Handling (Detected by SonarQube & Trivy)
    @GetMapping("/read-file")
    public String readFile(@RequestParam String filePath) {
        try {
            File file = new File(filePath); // 🚨 Path Traversal risk
            Scanner scanner = new Scanner(new FileInputStream(file));
            return scanner.nextLine();
        } catch (IOException e) {
            return "File read error"; // 🚨 No proper logging
        }
    }
}
