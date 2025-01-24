package com.on.eye.api.auth.model.vo;

import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.common.model.vo.ImageVo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileVo {
    private final Long id;
    private final String nickname;
    private final String email;
    private final ImageVo profileImage;

    public static UserProfileVo from(User user) {
        return UserProfileVo.builder()
                .id(user.getId())
                .nickname(user.getProfile().getNickname())
                .email(user.getProfile().getEmail())
                .profileImage(user.getProfile().getProfileImageUrl())
                .build();
    }
}
