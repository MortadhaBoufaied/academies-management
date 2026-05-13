package com.footballacademy.DTO;

import com.footballacademy.model.User;

public
class UserWithRoleDTO {
    private User user;
    private String role;
    private Object roleDetails;
    public UserWithRoleDTO(User user, String role, Object roleDetails) {
        this.user = user;
        this.role = role;
        this.roleDetails = roleDetails;
    }
    public User getUser() {
        return user;
    }
    public String getRole() {
        return role;
    }
    public Object getRoleDetails() {
        return roleDetails;
    }
}
