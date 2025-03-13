package com.on.eye.api.auth.model.dto;

import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.global.common.model.vo.ImageVo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProfileViewDto {
    private final Long id;
    private final ImageVo profileImage;
    private final String nickname;

    @Builder
    public ProfileViewDto(
            Long id, String email, String phoneNumber, ImageVo profileImage, String nickname) {
        this.id = id;
        this.profileImage = profileImage;
        this.nickname = nickname;
    }

    public static ProfileViewDto from(User user) {
        return ProfileViewDto.builder()
                .id(user.getId())
                .nickname(user.getProfile().getNickname())
                .profileImage(user.getProfile().getProfileImageUrl())
                .build();
    }
}
