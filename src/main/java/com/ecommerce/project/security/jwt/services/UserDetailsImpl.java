package com.ecommerce.project.security.jwt.services;

import com.ecommerce.project.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private  static final long serialVersionUID =1L;
    //this always checks the class structure
    private String username;
    private Long id;
    @JsonIgnore
    private String password;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String username, Long id, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.id = id;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }


    public static UserDetailsImpl build(User user){
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getUserName(),
                user.getUserId(),
                user.getPassword(),
                user.getEmail(),
                authorities
        );
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id,user.id);
    }
}
