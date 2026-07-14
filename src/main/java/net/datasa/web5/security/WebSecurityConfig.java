package net.datasa.web5.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * 시큐리티 환경설정 클래스
 */
@Slf4j
@Configuration					// Spring이 이 클래스를 설정 클래스로 인식하여 빈 등록 대상으로 만듬
@EnableWebSecurity  			// Spring Security 활성화
@EnableMethodSecurity			// "메서드 단위 보안" 기능을 활성화하는 어노테이션
@RequiredArgsConstructor
public class WebSecurityConfig {
	
    //로그인 없이 접근 가능 경로
    private static final List<String> PUBLIC_URLS = List.of (
            "/"                    // root
			, "/favicon.ico"         		// 브라우저 자동 요청 파비콘
			, "/images/**"          		// 정적리소스 - Image
			, "/css/**"             		// 정적리소스 - CSS
			, "/js/**"              		// 정적리소스 - JS
			, "/security"					// 시큐리티 타임리프 문법
			, "/member/inactive"			// 휴면계정 해제
            , "/member/join"        		// 회원가입
            , "/member/idCheck"     		// ID중복확인
            , "/member/info"        		// 개인정보 수정
            , "/admin/create-admin"        	// 관리자 계정 생성
            , "/admin/page"        			// 관리자 페이지(권한 비교)
            , "/board/listAll"      		// 게시판 전체 목록 보기
            , "/board/list"         		// 게시판 목록 보기
            , "/board/read"         		// 게시판 글 읽기
			, "/board/download"				// 파일 다운로드
			, "/board/preview"				// 이미지 미리보기
			, "/error/**"
	);
	
	/**
	 * 시큐리티 필터 설정 메서드
	 * - 인증 및 인가, 로그인/로그아웃, CORS, CSRF 설정 등을 포함함
	 */
    @Bean
    protected SecurityFilterChain config(HttpSecurity http) throws Exception {
        http
			// 1. CSRF, CORS, HttpBasic 등 보안 기본 비활성화 처리
			// 개발 단계: 개발 중에는 CORS와 CSRF 보호를 비활성화하여 빠른 테스트 및 디버깅
			.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			//브라우저 기본 로그인 인증창 비활성화
			.httpBasic(AbstractHttpConfigurer::disable)
			
			// 2. URL별 권한 설정
			.authorizeHttpRequests(author -> author
				// 위에서 정의한 일반 public URL 허용
				.requestMatchers(PUBLIC_URLS.toArray(String[]::new)).permitAll()   			// 모두 접근 허용
                .requestMatchers("/member/loginForm", "/member/login").permitAll() 	// 명시 허용
				.anyRequest().authenticated()
            )
			
			// 3. 폼 로그인 설정
			.formLogin(formLogin -> formLogin
				.loginPage("/member/loginForm")         		// 로그인폼 페이지 경로
				.usernameParameter("id")                		// 폼의 ID 파라미터 이름
				.passwordParameter("password")          		// 폼의 비밀번호 파라미터 이름
				
				// Spring Security가 로그인 인증 처리를 수행하기 위해 사용하는 내부 경로
				// 사용자가 /member/login URL로 POST 요청을 보내면, Spring Security의 필터 체인에서 이 요청을 가로챔
				.loginProcessingUrl("/member/login")    		// 로그인폼 제출하여 처리할 경로
				.defaultSuccessUrl("/", true)		// 로그인 성공 시 이동할 경로
				.permitAll()                            		// 로그인 페이지는 모두 접근 허용
            )
				
			// 4. 로그아웃 설정
            .logout(logout -> logout
				.logoutUrl("/member/logout")            		// 로그아웃 처리 경로
				.invalidateHttpSession(true)     				// 세션 무효화
				.deleteCookies("JSESSIONID")   // 쿠키 자체 삭제
				.logoutSuccessUrl("/")                  		// 로그아웃 성공 시 이동할 경로
            )
			;

        return http.build();
    }
	
    /*
		암호화 하는 객체를 정의
		비밀번호 + 임의의 문자열(솔트) > 해싱함수 > 암호화
		단방향 해싱함수(각 이름별 함수)를 이용하기 때문에 복호화 불가능 (비교할 비밀번호와 일치하는지는 확인 가능)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
}
