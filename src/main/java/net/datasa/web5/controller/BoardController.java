package net.datasa.web5.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.service.BoardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
