package net.datasa.web5.service;

/*
	회원 서비스
 */

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.entity.MemberEntity;
import net.datasa.web5.repository.MemberRespository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
	
	// 회원 관리 Repository
	private final MemberRespository mr;
	
	// 암호화 객체
	private final BCryptPasswordEncoder passwordEncoder;
	
	//-----------------------------------------------------
	
	/**
	 * 가입시 아이디 중복 확인
	 * @param searchId 조회할 ID
	 * @return 해당 아이디로 가입 가능 여부
	 */
	public boolean idCheck(String searchId) {
		// 값이 있으면 같은 ID 사용 못 하도록
		return !mr.existsById(searchId);
	}
}
