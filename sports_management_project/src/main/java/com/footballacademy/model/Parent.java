package com.footballacademy.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
public
class Parent {
    /** Shared PK with User stored in column user_id */
    @Id
    @Column(name = "user_id")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> children = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_parent_academy"))
    private Academy academy;
    public Parent() {
    }
    public Parent(User user) {
        this.user = user;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public List<Player> getChildren() {
        return children;
    }
    public void setChildren(List<Player> children) {
        this.children = children;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public void addChild(Player player) {
        children.add(player);
        player.setParent(this);
    }
    public void removeChild(Player player) {
        children.remove(player);
        player.setParent(null);
    }
}
