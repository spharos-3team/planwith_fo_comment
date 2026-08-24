package com.planwith.planwith_fo_comment.adapter.in.web.internal;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_comment.application.port.in.GetCommentReportContextUseCase;
import com.planwith.planwith_fo_comment.application.query.CommentReportContextResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class CommentReportContextController {

	private final GetCommentReportContextUseCase getCommentReportContextUseCase;

	// 신고 대상 댓글 컨텍스트 조회
	@GetMapping("/{commentUuid}/report-context")
	public ResponseEntity<CommentReportContextResponse> getReportContext(@PathVariable UUID commentUuid) {
		log.info("CommentReportContextController : GET getReportContext : 신고 대상 댓글 컨텍스트 조회 - commentUuid={}",
				commentUuid);
		CommentReportContextResult result = getCommentReportContextUseCase.getReportContext(commentUuid);
		return ResponseEntity.ok(new CommentReportContextResponse(
				result.commentUuid(),
				result.authorMemberUuid(),
				result.reportable()
		));
	}

	public record CommentReportContextResponse(
			UUID commentUuid,
			UUID authorMemberUuid,
			boolean reportable
	) {
	}
}
