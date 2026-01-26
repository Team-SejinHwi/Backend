package com.neo.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_message")
public class ChatMessageEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // 어느 방 메시지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoom;

    private Long senderId;     // 보낸 사람 ID
    private String senderName; // 보낸 사람 이름

    @Column(columnDefinition = "TEXT")
    private String message;    // 메시지 내용

    @CreationTimestamp
    private LocalDateTime sendDate;
}