package com.on.eye.api.common.model.vo;

import static com.on.eye.api.constants.Constants.ASSET_DOMAIN;

import jakarta.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ImageVo {
    private String url;

    @JsonValue
    public String getUrl() {
        if (url == null) return null;

        if (url.contains("kakao")) return url;

        return ASSET_DOMAIN + url;
    }

    public ImageVo(String url) {
        this.url = url;
    }

    public static ImageVo of(String url) {
        return new ImageVo(url);
    }
}
