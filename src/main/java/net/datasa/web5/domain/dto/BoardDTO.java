package net.datasa.web5.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.web5.domain.entity.BoardEntity;

import java.time.LocalDateTime;
import java.util.List;

/*
	게시글 정보 DTO
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {

	private Integer boardNum;			// 게시글 번호
	private String memberId;			// 작성자 아이디
	private String memberName;			// 작성자 이름
	private String title;				// 게시글 제목
	private String contents;				// 게시글 내용
	private Integer viewCount;			// 조회수
	private Integer likeCount;			// 추천수
	private String originalName;		// 첨부파일의 원래 이름
	private String fileName;			// 첨부파일의 저장된 이름
	private LocalDateTime createDate;	// 작성 시간
	private LocalDateTime updateDate;	// 수정 시간
	private List<ReplyDTO> replyList;	// 댓글 목록
	
	public static BoardDTO convertToBoardDTO(BoardEntity entity) {
		return BoardDTO.builder()
				.boardNum(entity.getBoardNum())
				.memberId(entity.getMember().getMemberId())
				.memberName(entity.getMember().getMemberName())
				.title(entity.getTitle())
				.contents(entity.getContents())
				.viewCount(entity.getViewCount())
				.likeCount(entity.getLikeCount())
				.originalName(entity.getOriginalName())
				.fileName(entity.getFileName())
				.createDate(entity.getCreateDate())
				.updateDate(entity.getUpdateDate())
				.build();
	}
	
}
