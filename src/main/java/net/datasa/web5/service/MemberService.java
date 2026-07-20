package net.datasa.web5.service;

/*
	회원 서비스
 */

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.MemberDTO;
import net.datasa.web5.domain.entity.MemberEntity;
import net.datasa.web5.exception.PasswordException;
import net.datasa.web5.repository.MemberRespository;
import net.datasa.web5.security.AuthenticatedUserDetailsService;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
	
	// 회원 관리 Repository
	private final MemberRespository mr;
	
	// 암호화 객체
	private final BCryptPasswordEncoder passwordEncoder;
	
	// 인증 정보
	private final AuthenticatedUserDetailsService uds;
	
	//-----------------------------------------------------
	
	/**
	 * 가입시 아이디 중복 확인
	 *
	 * @param searchId 조회할 ID
	 * @return 해당 아이디로 가입 가능 여부
	 */
	public boolean idCheck(String searchId) {
		// 값이 있으면 같은 ID 사용 못 하도록
		return !mr.existsById(searchId);
	}
	//-----------------------------------------------------
	
	/**
	 * 회원 가입 처리
	 *
	 * @param dto 회원 정보
	 */
	public void join(MemberDTO dto) {
		MemberEntity entity = MemberEntity.builder()
				.memberId(dto.getMemberId())
//				.memberPassword(dto.getMemberPassword())
				.memberPassword(passwordEncoder.encode(dto.getMemberPassword()))
				.memberName(dto.getMemberName())
				.email(dto.getEmail())
				.phone(dto.getPhone())
				.address(dto.getAddress())
//				.enabled(true)
//				.rolename("ROLE_USER")
				.build();
		
		mr.save(entity);
	}
	//-----------------------------------------------------
	
	/**
	 * 휴면 계정 해제
	 *
	 * @param memberId
	 * @param memberPassword
	 */
	public void inactive(String memberId, String memberPassword) {
		// 아이디에 맞는 값을 테이블에서 가져옴
		MemberEntity entity = mr.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원 없음"));
		
		// 비밀번호 체크
		if (!passwordEncoder.matches(memberPassword, entity.getMemberPassword())) {
			throw new PasswordException("비밀번호가 일치하지 않습니다.");
		}
		entity.setEnabled(true);
		
	}
	//-----------------------------------------------------
	
	/**
	 * 휴면 해제 후 로그인 처리
	 *
	 * @param memberId
	 * @param request
	 */
	public void bypassLogin(String memberId, HttpServletRequest request) {
		
		// 1. DB로부터 회원정보를 조회해 인증 정보 객체(UserDetails) 가져오기
		UserDetails userDetails = uds.loadUserByUsername(memberId);
		
		// 2. Spring Security가 이해할 수 있는 인증 객체 생성
		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(
						userDetails, userDetails.getPassword(), userDetails.getAuthorities());
		
		// 3. SecurityContext 생성 후 주입
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);        // 인증 객체 등록
		
		// 4. '현재 요청을 처리 중인 스레드'의 보관함에 SecurityContext를 저장
		SecurityContextHolder.setContext(context);
		
		// 5. 세션에도 SecurityContext 저장
		HttpSession session = request.getSession(true);
		// SecurityContext를 Spring Security가 알아볼 수 있는 이름으로 저장
		session.setAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
		);
		
		Authentication info = SecurityContextHolder.getContext().getAuthentication();
		log.debug(">>> 직접 만든 인증객체 정보 : {}", info);
	}

//	public MemberDTO info(UserDetails user) {
//		MemberEntity entity = mr.findById(user.getUsername()).orElseThrow(() -> new EntityNotFoundException("회원 없음"));
//		MemberDTO dto = MemberDTO.builder()
//				.memberId(entity.getMemberId())
//				.memberName(entity.getMemberName())
//				.email(entity.getEmail())
//				.phone(entity.getPhone())
//				.address(entity.getAddress())
//				.build();
//		return dto;
//	}
	//---------------------------------------------------------------------------
	
	/**
	 * 회원정보 수정
	 *
	 * @param dto 수정할 회원정보
	 */
	public void update(MemberDTO dto) {
		MemberEntity entity = mr.findById(dto.getMemberId())
				.orElseThrow(() -> new EntityNotFoundException(dto.getMemberId() + ": 회원이 없습니다"));
		if (!dto.getMemberPassword().isEmpty()) {
			entity.setMemberPassword(passwordEncoder.encode(dto.getMemberPassword()));
		}
		entity.setMemberName(dto.getMemberName());
		entity.setEmail(dto.getEmail());
		entity.setPhone(dto.getPhone());
		entity.setAddress(dto.getAddress());
		
		mr.save(entity);
	}
	
	/**
	 * 회원정보 조회
	 *
	 * @param id 조회할 아이디
	 * @return     회원 한 명의 정보
	 */
	public MemberDTO getMember(String id) {
		MemberEntity entity = mr.findById(id).orElseThrow(() -> new EntityNotFoundException(id + ": 아이디가 없습니다."));
		MemberDTO dto = MemberDTO.builder()
				.memberId(entity.getMemberId())
				.memberName(entity.getMemberName())
				.email(entity.getEmail())
				.phone(entity.getPhone())
				.address(entity.getAddress())
				.enabled(entity.getEnabled())
				.rolename(entity.getRolename())
				.build();
		
		
		return dto;
	}
	
	// [ 관리자 ]
	//-----------------------------------------------------------------
	// 저장 테스트
	public void admin() {
		MemberEntity entity = MemberEntity.builder()
				.memberId("admin")
				.memberPassword(passwordEncoder.encode("1111"))
				.memberName("admin")
				.email("admin@admin.com")
				.phone("010-1234-5678")
				.address("none")
				.enabled(true)
				.rolename("ROLE_ADMIN")
				.build();
		
		mr.save(entity);
	}
	
	public List<MemberDTO> list() {
		List<MemberDTO> dtoList = new ArrayList<>();
		Sort sort = Sort.by(
				Sort.Order.asc("rolename"),
				Sort.Order.desc("memberName")
		);
		// ⬆️둘 다⬇️ 권한 오름차순, 이름 내림차순
		Sort sort1 = Sort.by(Sort.Direction.ASC, "rolename").and(Sort.by(Sort.Direction.DESC, "memberName"));
		for (MemberEntity entity : mr.findAll(sort1)) {
			MemberDTO dto = MemberDTO.builder()
					.memberId(entity.getMemberId())
					.memberName(entity.getMemberName())
					.email(entity.getEmail())
					.phone(entity.getPhone())
					.address(entity.getAddress())
					.rolename(entity.getRolename())
					.enabled(entity.getEnabled())
					.build();
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	/**
	 * 회원 검색 결과 조회
	 *
	 * @param keyword
	 * @return
	 */
	public List<MemberDTO> selectById(String keyword) {
		List<MemberEntity> resultEntityList = mr.findByMemberIdContaining(keyword);
		List<MemberDTO> resultDtoList = new ArrayList<>();
		for (MemberEntity entity : resultEntityList) {
			MemberDTO dto = MemberDTO.builder()
					.memberId(entity.getMemberId())
					.memberName(entity.getMemberName())
					.email(entity.getEmail())
					.phone(entity.getPhone())
					.address(entity.getAddress())
					.rolename(entity.getRolename())
					.enabled(entity.getEnabled())
					.build();
			resultDtoList.add(dto);
		}
		
		return resultDtoList;
	}
	
	public void role(String id) {
		MemberEntity entity = mr.findById(id).orElseThrow(() -> new EntityNotFoundException(id + "회원정보 없음"));
		if (entity.getRolename().equals("ROLE_ADMIN")) {
			entity.setRolename("ROLE_USER");
		} else {
			entity.setRolename("ROLE_ADMIN");
		}
		mr.save(entity);
	}
}
