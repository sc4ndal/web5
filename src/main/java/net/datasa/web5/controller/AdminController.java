package net.datasa.web5.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.MemberDTO;
import net.datasa.web5.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
	
	private final MemberService ms;
	
	//---------------------------------------------------------
	// 관리자 계정 생성 (테스트용)
	@GetMapping("/create-admin")
	public String createAdmin() {
		log.debug("> 관리자 계정 생성");
		ms.admin();		// id: admin / rolename: ROLE_ADMIN
		log.debug("생성완료");
		
		return "redirect:/";
	}
	//---------------------------------------------------------
	@GetMapping("/page")
	public String adminPage() {
		
		return "/adminView/adminPage";
	}
	//---------------------------------------------------------
	
	/**
	 * 회원 목록 페이지 이동 (검색 포함)
	 * @param keyword	검색할 ID
	 * @param model
	 * @return	memberList.html
	 		@PreAuthorize
			Spring Security에서 메서드 실행 전에 권한을 검사하는 어노테이션
	 		
	 		hasRole('ADMIN'), hasRole('USER')	해당 권한시 허용
	 		isAuthenticated()					로그인시(인증) 허용
	 		isAnonymous()						로그인 하지 않은 경우 허용
	 */
	@PreAuthorize("hasRole('ADMIN')")	// 권한 체크 - 관리자만 가능
	@GetMapping("/list")
	public String list(
			@RequestParam(value = "keyword", required = false) String keyword,
			Model model) {
		List<MemberDTO> dtoList = null;
		if (keyword == null || keyword.trim().isEmpty()) {
			dtoList = ms.list();
		} else {
			dtoList = ms.selectById(keyword);
		}
		
		model.addAttribute("memberList", dtoList);
		model.addAttribute("keyword", keyword);
		log.debug("회원목록: {}", dtoList);
		
		return "/adminView/memberList";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/role/{id}")
	public String role(
			@PathVariable("id") String id
	) {
		log.debug("가져온 값  : {}", id);
		ms.role(id);
		log.debug("권한 변경완료");
		return "redirect:/admin/list";
	}
}
