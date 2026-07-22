package net.datasa.web5.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
			log.debug("UserName : {}", user.getUsername());
			log.debug("Authoities: {}", user.getAuthorities());
			log.debug("상태 정보 - 만료 여부 : {} , 잠금여부 : {}, 비밀번호만료여부:{}, 활성화여부:{}"
					, user.isAccountNonExpired(), user.isAccountNonLocked(), user.isCredentialsNonExpired(), user.isEnabled());
			log.debug("============================");
		} else {
			log.debug("인증 실패 또는 미인증 상태");
		}
		return "security";
	}
	/*
		IOStream
		-	IOStream(Input/Output Stream)은 데이터를 읽고 쓰기 위한 통로
		-	입출력 대상 : 파일, 키보드, 네트워크, 메모리 등 다양한 자원이 될 수 있음
		File
		-	파일의 위치, 이름, 크기, 존재 여부 등의 정보를 다루는 클래스
		-	파일 내용을 읽거나 쓰는 역할은 하지 않고, 파일 자체(메타 데이터)를 관리
			(실제 파일 읽기/쓰기는 FileInputStream, FileOutputSteam 등 사용)
			
		File 관련 메서드
		-------------------------------------------------------------------
		exists()							파일이나 폴더가 존재하는지 확인
		isFile()							이 경로가 파일인지 확인
		isDirectory()						이 경로가 폴더(디렉토리)인지 확인
		mkdir()								폴더 생성
		mkdirs()							경로 중간에 없는 폴더까지 모두 생성
		delete()							파일이나 폴더 삭제
		getName()							파일 또는 폴더 이름 반환
		getPath(), getAbsolutePate()		경로 문자열 반환
		-------------------------------------------------------------------
	 */
	
	@Value("${board.uploadPath}")
	String uploadPath;                    // 파일 저장 경로
	
	@GetMapping("/file")
	public String file() {
		// uploadPath : c:/upload
		File dir = new File(uploadPath, "myfolder");
		
		// 1. 폴더 생성 : mkdirs()
		if (!dir.exists()) {
			boolean ok = dir.mkdirs();
			log.debug("폴더 생성 여부 : {} -> {}", ok, dir.getAbsolutePath());
			if (!ok) return "redirect:/";
		}
		
		// 2. 파일 객체 생성 (폴더 + 파일명)
		File file = new File(dir, "example.txt");
		
		// 3. 파일 정보 출력
		log.debug("=== [파일 정보] ===");
		log.debug("이름: {}", file.getName());
		log.debug("절대 경로: {}", file.getAbsolutePath());
		log.debug("존재 여부: {}", file.exists());
		
		// 4. IOStream 으로 "출력" - 파일 내용을 실제로 생성/저장
		String content = """
				안녕하세요!
				IOStream 으로 파일에 저장합니다.
				""";
		try (
				// 바이트로 파일에 출력
				OutputStream out = new FileOutputStream(file);
				// 바이트 -> 문자 (인코딩)
				Writer write = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				// 성능(버퍼)
				BufferedWriter bw = new BufferedWriter(write);
		) {
			bw.write(content);
			bw.flush();            // 지금까지 모인 데이터가 버퍼에 있는걸 파일에 밀어넣기
			log.debug("파일 쓰기 완료");
			
		} catch (IOException e) {
			log.debug("파일 쓰기 실패: {}", e.getMessage());
			
		}
		
		// 5. IOStream 으로 "입력"
		try (
				InputStream in = new FileInputStream(file);
				Reader read = new InputStreamReader(in, StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(read);
				) {
			log.debug("=== [파일 내용 읽기] ===");
			String line;
			while ((line = br.readLine()) != null) {
				log.debug("내용: {}", line);
			}
		} catch (IOException e) {
			log.debug("파일 읽기 실패: {}", e.getMessage());
		}
		
		// 6. 삭제
		boolean deleted = file.delete();
		log.debug("파일 삭제 여부: {}", deleted);
		
		return "redirect:/";
	}
}
