package net.datasa.web5.repository;

import net.datasa.web5.domain.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
	게시판 Repository
 */
@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
}
