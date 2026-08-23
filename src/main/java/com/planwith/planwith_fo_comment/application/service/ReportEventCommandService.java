package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.HandleReportCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentReportedUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentReportProjectionPort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEventCommandService implements HandleCommentReportedUseCase {
	private static final String REPORT_TARGET_TYPE = "REPORT";

	private final CommentCommandPort commentCommandPort;
	private final CommentReportProjectionPort commentReportProjectionPort;
	private final ProcessedCommentEventService processedCommentEventService;

	@Override
	@Transactional
	public void handleReported(HandleReportCommand command) {
		StoryComment comment = commentCommandPort.findByUuidForUpdate(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn("Report event ignored because comment is unavailable - commentUuid={}", command.commentUuid());
			return;
		}
		if (processedCommentEventService.isDuplicate(command.eventMetadata())) {
			log.warn("Duplicate report event ignored - eventUuid={}", eventUuid(command));
			return;
		}
		if (commentReportProjectionPort.existsByReportUuid(command.reportUuid())) {
			record(command);
			return;
		}

		comment.increaseReportCount();
		commentCommandPort.save(comment);
		commentReportProjectionPort.save(command.reportUuid(), command.commentUuid());
		record(command);
		log.info("CommentReported applied - eventUuid={}, reportUuid={}, commentUuid={}",
				eventUuid(command), command.reportUuid(), command.commentUuid());
	}

	private void record(HandleReportCommand command) {
		processedCommentEventService.record(REPORT_TARGET_TYPE, command.eventMetadata());
	}

	private Object eventUuid(HandleReportCommand command) {
		return command.eventMetadata() == null ? null : command.eventMetadata().eventUuid();
	}
}
