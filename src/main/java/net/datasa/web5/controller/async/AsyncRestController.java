package net.datasa.web5.controller.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@RestController
@Controller
@Slf4j
@RequestMapping("/api")
public class AsyncRestController {
	/*
		@ResponseBody
			- 메서드가 반환하는 자바 객체를 HTTP 응답 본문 데이터로 직접 변화하여 전달
			- HTTP 상태코드 및 응답 헤더를 유연하게 제어하기는 어려움
		@RestController
			- RESTful 웹 서비스에 사용되는 어노테이션으로,
			  @Controller + @ResponseBody 가 합쳐진 형태
			- 클래스에 붙이면 해당 클래스 내 모든 메서드에 @ResponseBody 가 가본 적용
	 */
	// 1. concept.html -------------------------------------------
	@ResponseBody
	@GetMapping("/basic")
	public String basicResponse() {return "Hello, Fetch World!";}
	// ResponseBody 가 있으면 return 부분 값이 HTML 로 그대로 문자열로 넘어감.
	
	
}
