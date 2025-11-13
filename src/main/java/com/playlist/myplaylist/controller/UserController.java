package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // src/main/resources/templates/login.html 뷰를 찾습니다.
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup"; // src/main/resources/templates/signup.html 뷰를 찾습니다.
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {

        if (userMapper.findByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/signup";
        }

        if (userMapper.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // 비밀번호 암호화

        userMapper.insertUser(user); // 이 메소드는 UserMapper에 추가해야 합니다.

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }
}
