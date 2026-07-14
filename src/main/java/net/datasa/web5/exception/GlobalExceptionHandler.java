package net.datasa.web5.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
