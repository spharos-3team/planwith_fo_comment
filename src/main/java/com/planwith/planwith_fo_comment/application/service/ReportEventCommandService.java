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

	private final CommentCommandPort commentCommandPort;
	private final CommentReportProjectionPort commentReportProjectionPort;

	@Override
	@Transactional
	public void handleReported(HandleReportCommand command) {
		log.info(
				"ReportEventCommandService : handleReported : CommentReported 소비 시작 - reportUuid={}, commentUuid={}",
				command.reportUuid(),
				command.commentUuid()
		);
		log.debug(
				"ReportEventCommandService : handleReported : CommentReported 요청 데이터 확인 - reportUuid={}, commentUuid={}, memberUuid={}",
				command.reportUuid(),
				command.commentUuid(),
				command.memberUuid()
		);

		if (commentReportProjectionPort.existsByReportUuid(command.reportUuid())) {
			log.warn(
					"ReportEventCommandService : handleReported : 중복 CommentReported 이벤트 무시 - reportUuid={}",
					command.reportUuid()
			);
			return;
		}

		StoryComment comment = commentCommandPort.findByUuid(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn(
					"ReportEventCommandService : handleReported : 로컬 댓글이 없어 report_count 반영을 건너뜀 - commentUuid={}",
					command.commentUuid()
			);
			return;
		}

		comment.increaseReportCount();
		commentCommandPort.save(comment);
		commentReportProjectionPort.save(command.reportUuid(), command.commentUuid());

		log.info(
				"ReportEventCommandService : handleReported : report_count 증가 완료 - commentUuid={}, reportCount={}",
				comment.getCommentUuid(),
				comment.getReportCount()
		);
	}
}
