package com.example.muzfit.model;

public enum Gender {
    FEMALE(0),
    MALE(1);

    private final int code;

    Gender(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return FEMALE;
    }
}
