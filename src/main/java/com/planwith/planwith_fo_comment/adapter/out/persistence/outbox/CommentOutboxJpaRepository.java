package com.planwith.planwith_fo_comment.adapter.out.persistence.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_comment.domain.outbox.OutboxStatus;

public interface CommentOutboxJpaRepository extends JpaRepository<CommentOutboxJpaEntity, UUID> {

	List<CommentOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
