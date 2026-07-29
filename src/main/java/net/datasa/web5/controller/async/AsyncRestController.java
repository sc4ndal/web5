package net.datasa.web5.controller.async;

import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.async.StudentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
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
//	@ResponseBody
	@GetMapping("/basic")
	public String basicResponse() {
		return "Hello, Fetch World!";
	}
	// ResponseBody 가 있으면 return 부분 값이 HTML 로 그대로 문자열로 넘어감.
	
	// 2. text.html -------------------------------------------
	@GetMapping("/text/send")
	public void asyncResponse(@RequestParam("msg") String msg) {
		log.debug("클라이언트가 보낸 텍스트: {}", msg);
	}
	
//	@ResponseBody
	@GetMapping("/text/receive")
	public String textReceive() {
		return "서버가 보낸 메시지";
	}
	
//	@ResponseBody
	@PostMapping("/text/exchange")
	public String textExchange(
			@RequestParam("n1") int n1,
			@RequestParam("n2") int n2
	) {
		return "두 수의 합은 " + (n1 + n2) + "입니다.";
	}
	
	// 3. object.html --------------------------------------
	// @RequestBody : HTTP 요청의 본문에 담긴 데이터를 자바 객체로 변환해주는 Annotation
	@PostMapping("/object/send")
	public void objectSend(
			@RequestBody StudentDTO student
	) {
		
		log.debug("수신된 학생 정보: {}", student);
	}
	
//	@ResponseBody
	@GetMapping("/object/receive")
	public StudentDTO receive() {
		return new StudentDTO("손흥민", 33);
	}
	
	@PostMapping("/object/exchange")
	public ResponseEntity<?> exchange(
			@RequestBody StudentDTO student
	) {
		log.debug("변경 전: {}", student);
		if(student.name() == null || student.name().trim().isEmpty()) {
			return ResponseEntity.badRequest()
					.body("이름은 필수 입력 항목입니다.");
		}
		
		return ResponseEntity.ok(new StudentDTO("홍길동", 100));
	}
	
}
