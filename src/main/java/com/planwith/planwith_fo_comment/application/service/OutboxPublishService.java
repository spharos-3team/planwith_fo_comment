package com.planwith.planwith_fo_comment.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.port.in.PublishOutboxUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentEventPublisherPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentOutboxPort;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublishService implements PublishOutboxUseCase {

	private static final int DEFAULT_BATCH_SIZE = 50;

	private final CommentOutboxPort commentOutboxPort;
	private final CommentEventPublisherPort commentEventPublisherPort;

	@Override
	@Transactional
	public void publishPending() {
		List<CommentOutboxEvent> pendingEvents = commentOutboxPort.findPending(DEFAULT_BATCH_SIZE);
		if (pendingEvents.isEmpty()) {
			return;
		}

		log.info(
				"OutboxPublishService : publishPending : Outbox 발행 시작 - count={}",
				pendingEvents.size()
		);

		for (CommentOutboxEvent event : pendingEvents) {
			commentEventPublisherPort.publish(event);
			commentOutboxPort.markPublished(event.getOutboxUuid());
			log.debug(
					"OutboxPublishService : publishPending : Outbox 발행 완료 - outboxUuid={}, eventType={}",
					event.getOutboxUuid(),
					event.getEventType()
			);
		}

		log.info("OutboxPublishService : publishPending : Outbox 발행 완료");
	}
}
