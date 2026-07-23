package net.datasa.web5.repository;

import net.datasa.web5.domain.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/*
	게시판 Repository
 */
@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
	
	// 1. [ 제목 검색 ] ----------------------------------------------------------
	Page<BoardEntity> findByTitleContaining(String searchWord, Pageable pageable);
	
	// 2. [ 본문 검색 ] ----------------------------------------------------------
	Page<BoardEntity> findByContentsContaining(String searchWord, Pageable pageable);
	
	// 3. [ 아이디 검색 ] ---------------------------------------------------------
	// @EntityGraph: 엔티티 객체들의 연관관계 중 어디까지를 한번에 묶어서 가져올지 지정
	//				 Fetch Join 을 JPQL 작성 없이 어노테이션으로 제공.
	@EntityGraph(attributePaths = {"member"})
	Page<BoardEntity> findByMember_MemberId(String searchWord, Pageable pageable);
	
	// 4. [ 전체 검색 ] ---------------------------------------------------------
	@EntityGraph(attributePaths = {"member"})
	Page<BoardEntity> findByTitleContainingOrContentsContainingOrMember_MemberIdContaining(String title, String contents, String memberId, Pageable pageable);
	
	// // 4. [ 전체 검색 ] ---------------------------------------------------------
	// b. JPQL 여기서 sql 문법 사용하는 방법
	@Query("SELECT board " +
			" FROM BoardEntity board JOIN FETCH board.member m " +
			"WHERE board.title 		LIKE %:searchWord% " +
			"	OR board.contents 	LIKE %:searchWord% " +
			"	OR m.memberId 		LIKE %:searchWord% " +
			"ORDER BY board.boardNum DESC")
	Page<BoardEntity> searchAll(@Param("searchWord") String searchWord, Pageable pageable);
}
