package net.datasa.web5.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.service.BoardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
	게시판 관련 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

	private final BoardService bs;
	
	// applaction.properties 파일의 게시판 관련 설정값
	@Value("${board.pageSize}")
	int pageSize;
	@Value("${board.linkSize}")
	int linkSize;
	@Value("${board.uploadPath}")
	String uploadPath;
	
	//-------------------------------------------------------------------
	
	/**
	 * 글쓰기 페이지로 이동
	 * @return writeForm.html
	 */
	@GetMapping("/write")
	public String write() {
		return "boardView/writeForm";
	}
	
	/**
	 * 게시글 저장 처리
	 * @param boardDTO	작성한 글 정보(제목,내용)
	 * @param user		로그인한 사용자 정보
	 * @param upload	첨부파일
	 * @return			/board/list
	 */
	@PreAuthorize("isAuthenticated()")			// 로그인한 사용자만 가능
	@PostMapping("/write")
	public String write(
			@ModelAttribute BoardDTO boardDTO,
			@AuthenticationPrincipal UserDetails user,		// 인증객체로부터 가져온 유저 값
			@RequestParam(value = "upload", required = false) MultipartFile upload
			) {
		
		boardDTO.setMemberId(user.getUsername());
		log.debug("저장할 글 정보:{}", boardDTO);
		
		if (upload != null) {
			log.debug("Empty: {}", 		 upload.isEmpty());
			log.debug("파라미터 이름: {}", upload.getName());
			log.debug("파일명: {}", 		 upload.getOriginalFilename());
			log.debug("파일크기: {}", 	 upload.getSize());
			log.debug("파일종류: {}", 	 upload.getContentType());
			
			bs.write(boardDTO, uploadPath, upload);
		}
		
		return "redirect:/";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 게시판 글 목록 조회
	 * @param model
	 * @return listAll.html
	 */
	@GetMapping("/listAll")
	public String listAll(Model model) {
		List<BoardDTO> boardList = bs.getBoardList();
		model.addAttribute("boardList", boardList);
		return "boardView/listAll";
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 게시판 목록을 조회하고 페이징 및 검색 기능을 제공
	 * @param model
	 * @param page		현재 페이지 (default: 0)
	 * @param searchType	검색 대상 (default: "")
	 * @param searchWord	검색어 (default: "")
	 * @return
	 */
	@GetMapping("/list")
	public String list(
			Model model,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "searchType", defaultValue = "") String searchType,
			@RequestParam(name = "searchWord", defaultValue = "") String searchWord
	) {
		log.debug("application.properties 설정 값 : pageSize={}, linkSize={}", pageSize, linkSize);
		log.debug("요청 파라미터: page={}, searchType={}, searchWord={}", page, searchType, searchWord);
		/*
			Page<T>
			-	Spring Data JPA에서 페이징과 관련된 작업을 간편하게 처리하기 위한 인터페이스
			-	조회된 데이터 목록 + 전체 페이지수 / 전체 데이터 개수 등의 메타 데이터
				List<T>처럼 데이터를 담고 있으면서도, 페이지 관련 정보를 함께 제공
				
				메서드						설명
				---------------------------------------------------------------------
				getContent()				실제 데이터 리스트 반환 (List<T>)
				getTotalElements()			전체 항목 수
				getTotalpages()				전체 페이지 수
				getNumber()					현재 페이지 번호 (0부터 시작)
				hasNext(), isFirst() 등		페이지 네비게이션에 활용 가능..
		 */
		Page<BoardDTO> boardPage = bs.searchBoardList(page, pageSize, searchType, searchWord);
		
		model.addAttribute("boardPage", boardPage);
		model.addAttribute("page", page);
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchWord", searchWord);
		model.addAttribute("linkSize", linkSize);
		
		// Page 객체 정보
		log.debug("전체 개수: {}", boardPage.getTotalElements());
		log.debug("전체 페이지수: {}", boardPage.getTotalPages());
		log.debug("현재 페이지: {}", boardPage.getNumber());
		log.debug("페이지당 글 수: {}", boardPage.getSize());
		log.debug("이전 페이지 존재: {}", boardPage.hasPrevious());
		log.debug("다음 페이지 존재: {}", boardPage.hasNext());
		
		return "boardView/list";
	}
}
