package net.datasa.web5.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/*
	게시글 정보 Entity
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="board")
/*
	무한 순환참조 방지
	1. 발생 원인:
		BoardEntity <-> ReplyEntity 긴 양방향 연관관계가 설정돼있을때,
		Lombok의 @ToString 으로 인해 서로를 무한으로 호출해
		StackOverflowError 가 발생
	2. 해결 방법:
		@ToString(exclude = {"..","..",".."})
		> 연관관계 필드(@ManyToOne, @OneToMany)는 모두 Lombok 출력 대상에서 제외
		API 응답시 Entity를 직접 반환하지 않고 DTO로 변환하여 반환하는 것을 권장
 */
@ToString(exclude = {"member", "replyList", "likeList"})
public class BoardEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_num")
	private Integer boardNum;		// 게시글 번호
	
	@Column(name = "title", nullable = false, length = 1000)
	private String title;			// 글 제목
	
	@Column(name = "contents", nullable = false)
	private String contents;		// 글 내용
	
	@Column(name = "view_count", nullable = false)
	private Integer viewCount;		// 조회수
	
	@Column(name = "like_count", nullable = false)
	private Integer likeCount;		// 추천수
	
	@Column(name = "original_name")
	private String originalName;	// 첨부파일 원래 이름
	
	@Column(name = "file_name")
	private String fileName;		// 첨부파일의 저장 이름
	
	@CreatedDate
	@Column(name = "create_date", updatable = false)
	private LocalDateTime createDate;	// 작성 시간
	
	@Column(name = "update_date")
	private LocalDateTime updateDate;	// 수정 시간
	
	/*
		@ManyToOne (N:1 단방향/양방향 연관관계)
		-	JPA에서 Entity 간 연관관계를 맵핑할 때 외래키(FK)를 지정하는 어노테이션
		fetch = FetchType.LAZY (지연 로딩)
		-	DB의 연관된 Entity를 로딩하는 전략을 지정하는 속성
		-	성능 최적화 권장
		-	연관된 엔티티를 즉시 조회하지 않고, 실제 사용하는 시점에 DB 조회
		-	(EAGER 사용시 불필요한 조인과 N+1 쿼리 폭탄 발생 가능성 있음)
		
		@JoinColumn
		-	DB 테이블의 Foreign Key 컬럼명을 지정
		-	referencedColumnName 은 대상 테이블의 PK를 가리킴
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", referencedColumnName = "member_id")
	private MemberEntity member;		// 작성자 정보
	
	/*
		@OneToMany (1:N 양방향 연관관계)
		-	하나의 부모 엔티티가 여러 개의 엔티티를 참조
		-	mappedBy = "board" : ReplyEntity의 'board' 필드가 FK 를 관리함을 의미
		-	cascade = CascadeType.All : 연관 Entity 변경/삭제 시 해당 Entity도 함께 변경/삭제
		-	orphanRemoval = true : List에서 제거된 엔티티 객체를 DB에서 자동으로 DELETE 처리
	 */
	// 답글 리스트
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReplyEntity> replyList;
	
	// 좋아요 리스트
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BoardLikeEntity> likeList;
	
	// insert가 실행되기 전에 먼저 실행 == 초기화
	@PrePersist
	public void prePersist() {
		if(viewCount == null) {
			this.viewCount = 0;
		}
		if (likeCount == null) {
			this.likeCount = 0;
		}
	}
	
	// 조회수 증가 메서드
	public void increaseViewCount() {this.viewCount++;}
	// 추천수 증가 메서드
	public void increaseLikeCount() {this.likeCount++;}

}
