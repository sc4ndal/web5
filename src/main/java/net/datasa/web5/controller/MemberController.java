package net.datasa.web5.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
	회원 Controller
 */

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
	
	// 회원 관련 Service
	private final MemberService ms;
	
	//------------------------------------------------------------------
	
	/**
	 * 회원 가입 페이지로 이동
	 * @return joinForm.html
	 */
	@GetMapping("/join")
	public String join() {
		
		return "memberView/joinForm";
	}
	
	/**
	 * 아이디 중복 확인 페이지로 이동
	 * @return idCheck.html
	 */
	@GetMapping("/idCheck")
	public String idCheck() {
		return "memberView/idCheck";
	}
	
	/**
	 * 아이디 중복 확인
	 * @param searchId 검색할 ID
	 * @param model
	 * @return idCheck.html
	 */
	@PostMapping("/idCheck")
	public String idCheck(
			@RequestParam("searchId") String searchId,
			Model model) {
		log.debug("검색할 ID: {}", searchId);
		boolean result = ms.idCheck(searchId);
		
		model.addAttribute("searchId", searchId);
		model.addAttribute("result", result);
		
		return "memberView/idCheck";
	}
	
}
