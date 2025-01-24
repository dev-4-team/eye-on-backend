package com.on.eye.api.auth.model.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.on.eye.api.auth.model.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long id;
    private final String name;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    @Builder
    public CustomOAuth2User(
            Long id,
            String name,
            String email,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    public static CustomOAuth2User create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities =
                AuthorityUtils.createAuthorityList("ROLE_" + user.getRole());

        return CustomOAuth2User.builder()
                .id(user.getId())
                .name(user.getProfile().getNickname())
                .email(user.getProfile().getEmail())
                .authorities(authorities)
                .attributes(attributes)
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            CustomOAuth2User that = (CustomOAuth2User) obj;
            if (!this.getId().equals(that.getId())) {
                return false;
            } else {
                return this.getAuthorities().equals(that.getAuthorities())
                        && this.getAttributes().equals(that.getAttributes());
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.getId().hashCode();
        result = 31 * result + this.getAuthorities().hashCode();
        result = 31 * result + this.getAttributes().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Id: ["
                + this.getId()
                + "], Granted Authorities: ["
                + this.getAuthorities()
                + "], User Attributes: ["
                + this.getAttributes()
                + "]";
    }
}
