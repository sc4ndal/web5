package net.datasa.web5.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.domain.entity.BoardEntity;
import net.datasa.web5.domain.entity.MemberEntity;
import net.datasa.web5.exception.FileStorageException;
import net.datasa.web5.repository.BoardLikeRepository;
import net.datasa.web5.repository.BoardRepository;
import net.datasa.web5.repository.MemberRespository;
import net.datasa.web5.repository.ReplyRepository;
import net.datasa.web5.util.FileManager;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
	게시판 관련 서비스
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
	
	private final MemberRespository 	mr;
	private final BoardRepository 		br;
	private final ReplyRepository 		rr;
	private final BoardLikeRepository 	blr;
	private final FileManager 			fileManager;
	
	//------------------------------------------------------------------------------
	
	/**
	 * 게시글 저장
	 * @param boardDTO		저장할 글 정보
	 * @param uploadPath	파일 저장할 경로
	 * @param upload		업로드한 파일
	 */
	public void write(BoardDTO boardDTO, String uploadPath, MultipartFile upload) {
		
		MemberEntity memberEntity = mr.findById(boardDTO.getMemberId()).orElseThrow(() -> new EntityNotFoundException("회원이 없습니다."));
		
		String fileName = null;
		String originalName = null;
		
		if (upload != null && !upload.isEmpty()) {
			try {
				fileName = fileManager.saveFile(uploadPath, upload);
				originalName = upload.getOriginalFilename();
			} catch (IOException e) {
				// checked 예외인 IOException을 런타임 예외 종류로 전환
				throw new FileStorageException("파일이 없습니다.");
			}
		}
		
		BoardEntity entity = BoardEntity.builder()
				.member(memberEntity)
				.title(boardDTO.getTitle())
				.contents(boardDTO.getContents())
				.fileName(fileName)
				.originalName(originalName)
				.build();
		log.debug("저장되는 게시글 정보: {}", entity);
		br.save(entity);
		
	}
	
	public List<BoardDTO> getBoardList() {
		List<BoardDTO> listDTO = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "boardNum");
		List<BoardEntity> boardList = br.findAll(sort);
		for (BoardEntity entity : boardList) {
			BoardDTO dto = BoardDTO.convertToBoardDTO(entity);
			listDTO.add(dto);
		}
		log.debug("가져온값:{}", listDTO);
		return listDTO;
	}
	
	/**
	 * 검색 후 지정한 한 페이지 분량의 글 목록 조회
	 * @param page			현재 페이지
	 * @param pageSize		한 페이지당 글 수
	 * @param searchType	검색 대상 (title, contents, id, all)
	 * @param searchWord	검색어
	 * @return 한 페이지의 글 목록
	 */
	public Page<BoardDTO> searchBoardList(int page, int pageSize, String searchType, String searchWord) {
		// Page 객체는 번호가 0부터 시작
		page--;
		
		/*
			Pageable
		- 몇 번째 페이지를 몇 개씩, 어떤 정렬로 가져올 것인지 정의한 인터페이스
		- PageRequest.of(페이지 번호, 페이지 크기, 정렬 정보) 사용
		 */
		// 페이지 조회 조건 (현재 페이지, 페이지당 글 수, 정렬 순서, 정렬 기준 컬럼)
		Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "boardNum");
		Page<BoardEntity> entityPage = switch (searchType) {
			// 1. 제목에 searchWord가 포함된 데이터 조회
			case "title" -> br.findByTitleContaining(searchWord, pageable);
			// 2. 내용에 searchWord가 포함된 데이터 조회
			case "contents" -> br.findByContentsContaining(searchWord, pageable);
			// 3. 작성자 ID가 searchWord와 일치하는 데이터 조회
			case "id" -> br.findByMember_MemberId(searchWord, pageable);
			// 4. 제목, 내용, 작성자 ID 모두 포함하는 통합 검색
			case "all" -> br.searchAll(searchWord, pageable);
			//			  br.findByTitleContainingOrContentsContainingOrMember_MemberIdContaining(searchWord, searchWord, searchWord, pageable)
			// 5. 기본 전체 조회
			default -> br.findAll(pageable);
		};
		
		log.debug("조회된 결과: {}", entityPage.getContent());
		List<BoardDTO> dtoList = new ArrayList<>();
		for (BoardEntity entity : entityPage.getContent()) {
			BoardDTO dto = BoardDTO.convertToBoardDTO(entity);
			dtoList.add(dto);
		}
		Page<BoardDTO> boardDTOPage = new PageImpl<>(
				dtoList, 							// DTO로 변환된 List
				entityPage.getPageable(), 			// 몇 번째 페이지인지, 몇 개씩 조회했는지 등
				entityPage.getTotalElements()		// 조건에 맞는 전체 데이터 개수
		);
		
		return boardDTOPage;
	}
}
