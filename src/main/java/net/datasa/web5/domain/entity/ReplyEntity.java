package net.datasa.web5.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

	/*
		댓글 정보 엔티티
	 */

@Builder
@Getter
@Setter
@ToString(exclude = {"board", "member"})
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "reply")
public class ReplyEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reply_num")
	private Integer replyNum;		// 댓글 번호
	
	@Column(name = "contents", nullable = false, length = 2000)
	private String contents;		// 댓글 내용
	
	@CreatedDate
	@Column(name = "create_date")
	private LocalDateTime createDate;	// 작성 시간
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", referencedColumnName = "member_id")
	private MemberEntity member;		// 작성자 정보
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_num", referencedColumnName = "board_num")
	private BoardEntity board;			// 게시글 정보
	
}
