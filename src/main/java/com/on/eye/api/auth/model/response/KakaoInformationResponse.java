package com.on.eye.api.auth.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class KakaoInformationResponse {

    private String id;
    private Properties properties;
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class Properties {
        private String nickname;
        private String profileImageUrl;
        private String thumbnailImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class KakaoAccount {

        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonNaming(SnakeCaseStrategy.class)
        public static class Profile {
            private String profileImageUrl;
            private String thumbnailImageUrl;
            private String nickname;
        }

        public String getProfileImageUrl() {
            return profile.getProfileImageUrl();
        }
    }

    public String getProfileUrl() {
        return kakaoAccount.getProfileImageUrl();
    }
}
