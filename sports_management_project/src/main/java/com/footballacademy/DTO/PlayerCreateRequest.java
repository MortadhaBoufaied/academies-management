package com.footballacademy.DTO;

public
class PlayerCreateRequest {
    public String nom;
    public String email;
    public String tel;
    public String dateNaissance;
    // yyyy-MM-dd
    public String password;
    // required because User.mdp is not nullable
    public String position;
    public Integer age;
    public String nationalite;
    public Double height;
    public Double weight;
    public String imageUrl;
    public Long divisionId;
    public Long parentId;
    public Long trainerId;
}
