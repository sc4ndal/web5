package net.datasa.web5.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.domain.dto.MemberDTO;
import net.datasa.web5.security.AuthenticatedUser;
import net.datasa.web5.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
	 *
	 * @return joinForm.html
	 */
	@GetMapping("/join")
	public String join() {
		
		return "memberView/joinForm";
	}
	
	/**
	 * 회원 가입 처리
	 *
	 * @param member 가입 정보
	 * @return /
	 */
	
	@PostMapping("/join")
	public String join(MemberDTO member) {
		log.debug("전달된 정보 : {}", member);
		
		ms.join(member);
		log.debug("회원가입성공!");
		
		return "redirect:/";
	}
	
	/**
	 * 아이디 중복 확인 페이지로 이동
	 *
	 * @return idCheck.html
	 */
	@GetMapping("/idCheck")
	public String idCheck() {
		return "memberView/idCheck";
	}
	
	/**
	 * 아이디 중복 확인
	 *
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
	
	/**
	 * 로그인폼으로 이동
	 *
	 * @return loginForm.html
	 */
	@GetMapping("/loginForm")
	public String loginForm() {
		return "memberView/loginForm";
	}
	
	/**
	 * 휴면계정 해제 페이지로 이동
	 *
	 * @return inactive.html
	 */
	@GetMapping("/inactive")
	public String inactiveForm() {
		return "memberView/inactive";
	}
	
	/**
	 * 휴면 계정 해제
	 *
	 * @param memberId
	 * @param memberPassword
	 * @param model
	 * @return loginForm.html
	 */
	@PostMapping("/inactive")
	public String inactive(
			@RequestParam("memberId") String memberId,
			@RequestParam("memberPassword") String memberPassword,
			HttpServletRequest request,
			Model model) {
		
		// 1. 수동 로그인
		ms.inactive(memberId, memberPassword);
//		log.debug("휴면 해제 성공");
//		model.addAttribute("memberId", memberId);
//		model.addAttribute("msg", "휴면 계정이 해제되었습니다. 다시 로그인해주세요.");
//
//		return "memberView/loginForm";
		
		// 2. 휴면 해제 후 로그인
		// 인증정보를 저장할 세션이 필요
		// SecurityContext를 세션에 저장해야 Security가 "로그인 상태"라고 판단
		ms.bypassLogin(memberId, request);
		
		return "redirect:/";
	}

//	@GetMapping("/info")
//	public String info(
//			@AuthenticationPrincipal UserDetails user,
//			Model model
//	) {
//		MemberDTO dto = ms.info(user);
//		model.addAttribute("userId", dto.getMemberId());
//		model.addAttribute("userName", dto.getMemberName());
//		model.addAttribute("userEmail", dto.getEmail());
//		model.addAttribute("userPhone", dto.getPhone());
//		model.addAttribute("userAddress", dto.getAddress());
//		return "/memberView/infoForm";
//	}
	
	/**
	 * 개인정보 수정 페이지로 이동
	 *
	 * @param user  로그인한 사용자 정보(Security Context - Authentication)
	 * @param model
	 * @return infoForm.html
	 * @PreAuthorize 메서드 실행 "전"에 권한 검사
	 * @PostAuthorize 메서드 실행 "후"에 결과를 보고 권한 검사
	 */
	@PreAuthorize("isAuthenticated()")    // 로그인한 사용자만 접근 허용
	@GetMapping("/info")
	public String info(
			@AuthenticationPrincipal UserDetails user,
			Model model) {
		log.debug("로그인한 사용자 정보 : {}", user);
		
		// 회원 조회
		MemberDTO member = ms.getMember(user.getUsername());
		model.addAttribute("member", member);
		log.debug("[조회 성공] {}", member);
		
		return "memberView/infoForm";
	}
	//-------------------------------------------------------------------------
	
	/**
	 * 개인정보 수정 처리
	 *
	 * @param user 로그인한 사용자 정보
	 * @param dto  수정할 정보
	 * @return /
	 */
	@PreAuthorize("isAuthenticated()")        // 로그인한 사용자만 접근가능
	@PostMapping("/info")
	public String info(
			@AuthenticationPrincipal AuthenticatedUser user,
			MemberDTO dto
	) {
		
		log.debug("수정할 정보:{}", dto);
		dto.setMemberId(user.getUsername());
		ms.update(dto);
		log.debug("[수정 성공]");
		
		return "redirect:/";
	}
	
}
