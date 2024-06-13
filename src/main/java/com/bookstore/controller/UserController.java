package com.bookstore.controller;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.services.RoleService;
import com.bookstore.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/login")
    public String login(){
        return "user/login";
    }
    @GetMapping("/register")
    public String reigister(Model model){
        model.addAttribute("user", new User());
        return "user/register";
    }
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result , Model model)
    {
        if (result.hasErrors()){
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error: errors){
                model.addAttribute(error.getField()+"_error",error.getDefaultMessage());
            }
            return "user/register";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user/user-list";
    }

    @GetMapping("/admin/users/{id}/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editUser(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        return "user/user-edit";
    }

    @PostMapping("/admin/users/{id}/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateUserRoles(@PathVariable("id") Long id, @RequestParam("roles") List<Long> roleIds) {
        User user = userService.getUserById(id);
        List<Role> roles = roleService.getAllRolesByIds(roleIds);
        user.setRoles(roles);
        userService.saveUser(user);
        return "redirect:/admin/users";
    }


}
