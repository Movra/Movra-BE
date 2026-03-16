package com.example.morva.sharedkernel.file.storage.type;

import lombok.Getter;

@Getter
public enum ImageType {

    PROFILE("profile-image/");

    private final String prefix;

    ImageType(String prefix) {
        this.prefix = prefix;
    }
}
