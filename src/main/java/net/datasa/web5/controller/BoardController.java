package net.datasa.web5.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.service.BoardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
