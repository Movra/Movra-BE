package com.example.movra.sharedkernel.file.storage.type;

import lombok.Getter;

@Getter
public enum ImageType {

    PROFILE("profile-image/"),
    FUTURE("future-vision-image/");

    private final String prefix;

    ImageType(String prefix) {
        this.prefix = prefix;
    }
}
