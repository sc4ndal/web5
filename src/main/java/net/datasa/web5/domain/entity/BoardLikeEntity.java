package net.datasa.web5.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
	추천 이력 정보 엔티티
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"board", "member"})
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "board_like",
// 여러 컬럼을 묶은 복합 유니크 제약조건을 만들때 사용
		uniqueConstraints = @UniqueConstraint(columnNames = {"board_num", "member_id"}))

public class BoardLikeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "like_num")
	private Integer likeNum;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_num", nullable = false)
	private BoardEntity board;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;
	
	@CreatedDate
	@Column(name = "create_date")
	private LocalDateTime createDate;
	
}
