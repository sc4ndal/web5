package net.datasa.web5.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {
	
	@GetMapping({"", "/"})
	public String home() {
		return "home";
	}
	
	@GetMapping("/security")
	public String security(
		/*
			@AuthenticationPrincipal
			-	Spring Security가 직접 로그인한 사용자 정보를 주입
		 */
			@AuthenticationPrincipal UserDetails user
			) {
		if (user != null) {
			log.debug("=== [인증 객체 정보 출력] ===");
			log.debug("UserName : {}",  user.getUsername());
			log.debug("Authoities: {}", user.getAuthorities());
			log.debug("상태 정보 - 만료 여부 : {} , 잠금여부 : {}, 비밀번호만료여부:{}, 활성화여부:{}"
			,user.isAccountNonExpired(), user.isAccountNonLocked(), user.isCredentialsNonExpired(),user.isEnabled());
			log.debug("============================");
		} else {
			log.debug("인증 실패 또는 미인증 상태");
		}
		return "security";
	}
}
