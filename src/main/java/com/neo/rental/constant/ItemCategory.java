package com.neo.rental.constant;

import lombok.Getter;

@Getter
public enum ItemCategory {
    DIGITAL("디지털/가전"),
    FURNITURE("가구/인테리어"),
    KIDS("유아동/유아도서"),
    SPORTS("스포츠/레저"),
    WOMAN("여성의류"),
    MAN("남성의류"),
    BOOK("도서/티켓/음반"),
    GAME("게임/취미"),
    BEAUTY("뷰티/미용"),
    PET("반려동물용품"),
    ETC("기타 중고물품");

    private final String description;

    ItemCategory(String description) {
        this.description = description;
    }
}