package net.datasa.web5.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
	
	// 1. 권한이 없는 경우
	@ExceptionHandler({
			AccessDeniedException.class,
			AuthorizationDeniedException.class
	})
	public String handleAuthorizationDenied(Exception e, Model model) {
		log.debug("> [GlobalException] 권한 거부 예외 : {}", e.getMessage());
		model.addAttribute("message", "접근권한이 없습니다.");
		
		return "errorView/custom-error-page";
	}
	
	// 2. 데이터를 찾을 수 없는 경우
	@ExceptionHandler(EntityNotFoundException.class)
	public String handleNotFound(EntityNotFoundException e, Model model) {
		log.debug("> [GlobalException] EntityNotFoundException: {}", e.getMessage());
		model.addAttribute("message", e.getMessage());
		
		return "errorView/custom-error-page";
	}
	
	// 3. 비즈니스 로직 예외
	@ExceptionHandler({PasswordException.class, RecommendException.class})
	public String handleBusinessExceptions(Exception e, Model model) {
		log.debug("> [GlobalException] 비즈니스 예외 발생 : {}", e.getMessage());
		model.addAttribute("message", e.getMessage());
		return "errorView/custom-error-page";
	}
	
	// 4. 파일 입출력 및 시스템 오류
	@ExceptionHandler(FileStorageException.class)
	public String handleFileStorage(FileStorageException e, Model model) {
		log.debug("> [GlobalException] FileStorageException : {}", e.getMessage());
		model.addAttribute("message", e.getMessage());
		return "errorView/custom-error-page";
	}
	
	// 5. ResponseStatusException 처리
	@ExceptionHandler
	public ResponseEntity<String> handleResponseStatus(ResponseStatusException e) {
		log.debug("> [GlobalException] ResponseStatusException 발생: {}", e.getReason());
		
		String alertScript = String.format("""
				<script>
					alert('%s');
					history.back();
				</script>
				""", e.getReason());
		
		// 브라우저가 이 응답을 HTML/JS로 인식하도록 Header 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(
				new MediaType("text", "html", StandardCharsets.UTF_8)
		);
		
		// 오류는 발생했지만 200 OK 상태로 JS 전달 (페이지 전환없이 JS 실행)
		return new ResponseEntity<>(alertScript, headers, HttpStatus.OK);
	}
}
