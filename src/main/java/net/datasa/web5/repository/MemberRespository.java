package net.datasa.web5.repository;

import net.datasa.web5.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository<entity, key값 타입>
 */
public interface MemberRespository extends JpaRepository<MemberEntity, String> {
	
	/*
		JPA 메서드 네이밍 규칙
		Spring Data JPA 는 사용자가 인터페이스에 특정 규칙대로 메서드명만 작성하면,
		그 이름을 분석해서 SQL 쿼리를 실행하는 구현체를 자동으로 생성.
		
		ex.
			패턴				동작			예시
			findBy			조회			findByUsername(String name)
			deleteBy		삭제			deleteByEmail(String email)
			existsBy		존재 여부	existsById(int id)
			countBy			개수 조회	countByType(String type)
			findBy...And...	다중 조건	findByUsernameAndAge(String, int)
	 */
	
	// SELECT m FROM MemberEntity m WHERE m.memberId LIKE : keyword
	// SELECT * FROM member WHERE member_id LIKE '%keyword%
	List<MemberEntity> findByMemberIdContaining(String keyword);
}
