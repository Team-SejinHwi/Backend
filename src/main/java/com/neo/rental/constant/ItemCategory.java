package com.neo.rental.constant;

import lombok.Getter;

@Getter
public enum ItemCategory {
    // [렌탈 수요 Top]
    DIGITAL("디지털/가전"),       // 노트북, 태블릿, 모니터
    LIVING("생활/주방"),         // 생활, 주방
    CAMERA("카메라/촬영장비"),     // DSLR, 삼각대, 조명, 액션캠 (★추가)
    CAMPING("캠핑/레저"),         // 텐트, 타프, 캠핑의자, 아이스박스 (★추가)
    TOOL("공구/산업용품"),        // 전동드릴, 사다리, 청소기 (★추가)

    // [일반 카테고리]
    SPORTS("스포츠/헬스"),        // 골프채, 테니스라켓, 헬스기구
    PARTY("파티/이벤트"),         // 코스튬, 행사용품, 빔프로젝터 (★추가)
    CLOTHING("의류/잡화"),        // 정장, 드레스, 명품가방, 시계 (성별 통합)
    KIDS("유아동/장난감"),        // 유모차, 카시트, 장난감
    FURNITURE("가구/인테리어"),    // 1인용 가구, 조명, 이사용 박스
    BOOK("도서/음반/티켓"),       // 전공서적, 만화책 전권
    GAME("게임/취미"),            // 닌텐도, 플스, 보드게임

    // [기타]
    BEAUTY("뷰티/미용"),          // LED마스크, 고가 미용기기
    PET("반려동물용품"),          // 이동장, 유모차
    ETC("기타");

    private final String description;

    ItemCategory(String description) {
        this.description = description;
    }
}