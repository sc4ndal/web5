package net.datasa.web5.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.domain.dto.ReplyDTO;
import net.datasa.web5.domain.entity.BoardEntity;
import net.datasa.web5.domain.entity.BoardLikeEntity;
import net.datasa.web5.domain.entity.MemberEntity;
import net.datasa.web5.domain.entity.ReplyEntity;
import net.datasa.web5.exception.FileStorageException;
import net.datasa.web5.exception.RecommendException;
import net.datasa.web5.repository.BoardLikeRepository;
import net.datasa.web5.repository.BoardRepository;
import net.datasa.web5.repository.MemberRespository;
import net.datasa.web5.repository.ReplyRepository;
import net.datasa.web5.util.FileManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
	게시판 관련 서비스
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
	
	private final MemberRespository mr;
	private final BoardRepository br;
	private final ReplyRepository rr;
	private final BoardLikeRepository blr;
	private final FileManager fileManager;
	
	//------------------------------------------------------------------------------
	
	/**
	 * 게시글 저장
	 *
	 * @param boardDTO   저장할 글 정보
	 * @param uploadPath 파일 저장할 경로
	 * @param upload     업로드한 파일
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
	 *
	 * @param page       현재 페이지
	 * @param pageSize   한 페이지당 글 수
	 * @param searchType 검색 대상 (title, contents, id, all)
	 * @param searchWord 검색어
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
				dtoList,                            // DTO로 변환된 List
				entityPage.getPageable(),            // 몇 번째 페이지인지, 몇 개씩 조회했는지 등
				entityPage.getTotalElements()        // 조건에 맞는 전체 데이터 개수
		);
		
		return boardDTOPage;
	}
	
	/**
	 * 게시글 1개 조회
	 *
	 * @param boardNum 글 번호
	 * @return 글 정보
	 */
	public BoardDTO getBoard(int boardNum) {
		BoardEntity boardEntity = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("글 없음"));
		
		// 조회수 증가
		boardEntity.increaseViewCount();
		
		// DTO 변환
		BoardDTO dto = BoardDTO.convertToBoardDTO(boardEntity);
		
		// 댓글 정보 (댓글 + 작성자(member)까지 한번에 조회 + 최신순 정렬)
		List<ReplyEntity> replyList = rr.findByBoard_BoardNumOrderByCreateDateDesc(boardNum);
		List<ReplyDTO> replyDTOList = new ArrayList<>();
		for (ReplyEntity reply : replyList) {
			replyDTOList.add(ReplyDTO.convertToReplyDTO(reply));
		}
		
		// 댓글 목록 추가
		dto.setReplyList(replyDTOList);
		
		return dto;
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 파일 다운로드
	 *
	 * @param boardNum   글 번호
	 * @param uploadPath 파일 저장 경로
	 * @return 클라이언트로 보낼 *상태코드 + HTTP 헤더 + 응답 데이터(Body)* 객체
	 */
	public ResponseEntity<Resource> download(int boardNum, String uploadPath) {
		
		// 1. DB 조회
		BoardEntity board = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
		
		if (board.getFileName() == null || board.getOriginalName() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다.");
		}
		
		// 2. 파일 경로 안전 검증
		// - get()	   : 문자열로 된 파일 경로를 자바가 이해할 수 있는 Path 객체로 변환/생성
		// - resolve() : 경로 및 파일명을 안전하게 붙여주는 역할
		Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
		Path filePath = baseDir.resolve(board.getFileName()).normalize();
		
		if (!filePath.startsWith(baseDir)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다."
			);
		}
		if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, "파일이 서버에 없습니다."
			);
		}
		
		// 3. 파일명 인코딩 (한글, 공백, 따옴표 문제 자동 해결)
		// ContentDisposition : 한글 파일명 깨짐이나 브라우저 호환성 문제를 해결하기 위해
		//						HTTP Content-Disposition 헤더를 자바 객체로 제공하는 전용 클래스
		ContentDisposition contentDisposition
				= ContentDisposition.builder("attachment")
				.filename(board.getOriginalName(), StandardCharsets.UTF_8)
				.build();
		
		/*
			Resource : 다양한 위치에 존재하는 파일 및 데이터 자원을 읽어오기 위한
					   통일된 인터페이스 (파일, URL, 클래스패스 등의 다양한 자원)
			MediaType : HTTP 요청 및 응답에서 주고 받는 데이터의 포멧(형식)을 제공하는 클래스
						인터넷 표준 규격인 MIME type을 자바 코드로 안전하게 제공
		 */
		// 4. Resource 생성
		Resource resource = new FileSystemResource(filePath);
		
		/*
			MIME (Multipurpose Internet Mail Extensions)
			- 이 데이터가 어떤 종류의 데이터인지 설명하는 표준 규칙
			
			MIME 타입								의미
			-------------------------------------------------------------------
			text/html								HTML 문서
			text/plain								일반 텍스트
			application/json						JSON 데이터
			image/jpeg								JPEG 이미지
			application/octet-stream				임의의 바이너리 데이터
		 */
		
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
				.contentLength(filePath.toFile().length())
				.body(resource);
	}
	
	public ResponseEntity<Resource> preview(int boardNum, String uploadPath) {
		// 1. DB 조회
		BoardEntity board = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
		
		if (board.getFileName() == null || board.getOriginalName() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다.");
		}
		
		// 2. 파일 경로 안전 검증
		// - get()	   : 문자열로 된 파일 경로를 자바가 이해할 수 있는 Path 객체로 변환/생성
		// - resolve() : 경로 및 파일명을 안전하게 붙여주는 역할
		Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
		Path filePath = baseDir.resolve(board.getFileName()).normalize();
		
		if (!filePath.startsWith(baseDir)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다."
			);
		}
		if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, "파일이 서버에 없습니다."
			);
		}
		
		try {
			// 3. MIME 타입 분석
			// probeContentType : 이 파일이 무슨 종류의 파일인지(이미지, PDF, 텍스트 인지 체크)
			String contentType = Files.probeContentType(filePath);
			if (contentType == null) {
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
			
			// 4. ContentDisposition
			ContentDisposition contentDisposition =
					ContentDisposition.builder("inline")
							.filename(board.getOriginalName(), StandardCharsets.UTF_8)
							.build();
			
			// 5. Resource 생성
			Resource resource = new FileSystemResource(filePath);
			
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
					.contentLength(filePath.toFile().length())
					.body(resource);
			
		} catch (IOException e) {
			log.error("파일 미리보기 처리 중 오류 발생 - 파일명 : {}", board.getFileName());
			throw new FileStorageException("파일 미리보기 처리 중 오류 발생");
		}
		
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 게시글 추천수 증가
	 *
	 * @param boardNum
	 * @param username
	 */
	public void like(int boardNum, String username) {
		BoardEntity board = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
		
		MemberEntity member = mr.findById(username).orElseThrow(() -> new EntityNotFoundException("유저 없음"));
		
		// 이미 추천한 이력이 있는지 확인
		Optional<BoardLikeEntity> alreadyLike = blr.findByBoardAndMember(board, member);
		
		if (alreadyLike.isPresent()) {
			// 이미 추천했다면 예외 던짐
			throw new RecommendException("이미 추천한 게시글입니다.");
		}
		BoardLikeEntity boardLike = BoardLikeEntity.builder()
				.member(member)
				.board(board)
				.build();
		blr.save(boardLike);
		
		// 추천수 증가
		board.increaseLikeCount();
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 게시글 삭제
	 *
	 * @param boardNum   삭제할 글 번호
	 * @param username   로그인한 아이디
	 * @param uploadPath 삭제할 첨부파일 경로
	 */
	public void delete(int boardNum, String username, String uploadPath) {
		BoardEntity boardEntity = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("게시글없음"));
		
		// 게시글 삭제 처리
		if (!boardEntity.getMember().getMemberId().equals(username)) {
			throw new RuntimeException("삭제 권한 없음");
		}
		br.delete(boardEntity);
		
		// 파일 삭제
		try {
			if (boardEntity.getFileName() != null) {
				boolean result = fileManager.deleteFile(uploadPath, boardEntity.getFileName());
				if (!result) {
					log.debug("> 삭제 대상 파일이 이미 없음.");
				}
				
			}
		} catch (IOException e) {
			throw new FileStorageException("파일이 없습니다.");
		}
	}
	
	//-------------------------------------------------------------------------
	public BoardDTO select(int boardNum) {
		BoardEntity entity = br.findById(boardNum).orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
		BoardDTO dto = BoardDTO.convertToBoardDTO(entity);
		return dto;
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 게시글 수정 처리
	 *
	 * @param dto        수정할 글 정보
	 * @param uploadPath 파일 경로
	 * @param upload     업로드된 파일
	 */
	public void update(BoardDTO dto, String uploadPath, MultipartFile upload) {
		BoardEntity entity = br.findById(dto.getBoardNum()).orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
		
		if (!entity.getMember().getMemberId().equals(dto.getMemberId())) {
			throw new RuntimeException("수정 권한이 없습니다.");
		}
		
		// 전달된 정보 수정
		entity.setTitle(dto.getTitle());
		entity.setContents(dto.getContents());
		entity.setUpdateDate(LocalDateTime.now());
		
		// 업로드된 파일 유무에 따라 기존 파일 삭제 후 새로 저장
		if (upload != null && !upload.isEmpty()) {
			// 파일 삭제
			try {
				if (entity.getFileName() != null) {
					fileManager.deleteFile(uploadPath, entity.getFileName());
					
				}
				String fileName = fileManager.saveFile(uploadPath, upload);
				entity.setOriginalName(upload.getOriginalFilename());
				entity.setFileName(fileName);
			} catch (IOException e) {
				throw new FileStorageException("파일이 없습니다.");
			}
		}
		
	}
}
