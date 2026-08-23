package com.planwith.planwith_fo_comment.adapter.out.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.in.PublishOutboxUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.publisher-enabled", havingValue = "true")
public class OutboxPublishScheduler {

	private final PublishOutboxUseCase publishOutboxUseCase;

	@Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
	public void publishPendingOutbox() {
		log.debug("OutboxPublishScheduler : publishPendingOutbox : Outbox 발행 스케줄 실행");
		publishOutboxUseCase.publishPending();
	}
}
