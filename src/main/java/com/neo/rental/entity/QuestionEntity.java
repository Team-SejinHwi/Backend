package com.neo.rental.entity;

import com.neo.rental.constant.QuestionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "question_table")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    // 작성자 (Member)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 관련 상품 (Item) - NULL 허용 (일반 문의일 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEntity item;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 비밀글 여부 (Default: false)
    @Builder.Default
    @Column(nullable = false)
    private boolean isSecret = false;

    // 답변 상태 (Default: WAITING)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20, nullable = false)
    private QuestionStatus status = QuestionStatus.WAITING;

    // 조회수 (Default: 0)
    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

    // [편의 기능] 질문에 달린 답변들 (양방향 매핑)
    // CascadeType.ALL: 질문 삭제 시 답변도 같이 삭제됨
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnswerEntity> answers = new ArrayList<>();

    // 조회수 증가 메소드
    public void upViewCount() {
        this.viewCount++;
    }

    // 상태 변경 메소드 (답변 달릴 때 사용)
    public void changeStatus(QuestionStatus status) {
        this.status = status;
    }
}