package net.datasa.web5.repository;

import net.datasa.web5.domain.entity.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
	댓글 Repository
 */
@Repository
public interface ReplyRepository extends JpaRepository<ReplyEntity, Integer> {
}
