package com.zzg.psychologyai.domain.model;

import com.zzg.psychologyai.domain.common.model.AggregateRoot;
import com.zzg.psychologyai.domain.enums.GenderEnum;
import com.zzg.psychologyai.domain.enums.UserStatusEnum;
import com.zzg.psychologyai.domain.enums.UserTypeEnum;
import com.zzg.psychologyai.domain.event.UserLoggedInEvent;
import com.zzg.psychologyai.domain.event.UserRegisteredEvent;
import com.zzg.psychologyai.domain.valueobject.Email;
import com.zzg.psychologyai.domain.valueobject.Password;
import com.zzg.psychologyai.domain.valueobject.Phone;
import com.zzg.psychologyai.domain.valueobject.Username;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户领域模型（聚合根）：对应 docs/ai-agent.sql 的 user 表。
 * <p>
 * 核心标识与安全字段以值对象承载，性别/类型/状态以枚举承载，
 * 保证聚合内不存在非法值；审计字段由基础设施层持久化实体负责。
 */
public class User extends AggregateRoot {

    private Long id;
    private Username username;
    private Email email;
    private Phone phone;
    private Password password;
    private String nickname;
    private String avatar;
    private GenderEnum gender;
    private LocalDate birthday;
    private UserTypeEnum userType;
    private UserStatusEnum status;

    public void markRegistered() {
        String emailText = this.email == null ? null : this.email.value();
        registerEvent(UserRegisteredEvent.of(this.id, this.username.value(), emailText));
    }

    public void markLoggedIn() {
        registerEvent(UserLoggedInEvent.of(this.id, this.username.value()));
    }

    public void rename(String newNickname) {
        Objects.requireNonNull(newNickname, "昵称不能为空");
        this.nickname = newNickname;
    }

    // ---------- getter / setter ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Username getUsername() {
        return username;
    }

    public void setUsername(Username username) {
        this.username = username;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public UserTypeEnum getUserType() {
        return userType;
    }

    public void setUserType(UserTypeEnum userType) {
        this.userType = userType;
    }

    public UserStatusEnum getStatus() {
        return status;
    }

    public void setStatus(UserStatusEnum status) {
        this.status = status;
    }

    // ---------- 聚合根按 ID 判等 ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user)) {
            return false;
        }
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username=" + username + ", status=" + status + '}';
    }
}
