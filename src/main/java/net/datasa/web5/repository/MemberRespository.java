package net.datasa.web5.repository;

import net.datasa.web5.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository<entity, key값 타입>
 */
public interface MemberRespository extends JpaRepository<MemberEntity, String> {
}
