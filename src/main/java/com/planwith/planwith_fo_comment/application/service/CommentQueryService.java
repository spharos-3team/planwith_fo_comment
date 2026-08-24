package com.planwith.planwith_fo_comment.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.port.in.GetCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentReportContextUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentsByStoryUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetManagedCommentsUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentQueryPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.CommentReportContextResult;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;
import com.planwith.planwith_fo_comment.application.query.GetManagedCommentsQuery;
import com.planwith.planwith_fo_comment.application.query.ManagedCommentResult;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentUseCase, GetCommentsByStoryUseCase, GetManagedCommentsUseCase,
		GetCommentReportContextUseCase {

	private final CommentQueryPort commentQueryPort;
	private final CommentThreadAssembler commentThreadAssembler;
	private final CommentManagementAuthorizer commentManagementAuthorizer;

	@Override
	public CommentQueryResult get(GetCommentQuery query) {
		log.debug(
				"CommentQueryService : get : 댓글 상세 조회 - commentUuid={}",
				query.commentUuid()
		);
		return commentQueryPort.findActiveByUuid(query.commentUuid())
				.orElseThrow(() -> new CommentNotFoundException(query.commentUuid()));
	}

	@Override
	public CommentReportContextResult getReportContext(UUID commentUuid) {
		log.debug("CommentQueryService : getReportContext : 신고 대상 댓글 조회 - commentUuid={}", commentUuid);
		return commentQueryPort.findReportContextByUuid(commentUuid)
				.orElseThrow(() -> new CommentNotFoundException(commentUuid));
	}

	@Override
	public List<CommentThreadResult> getByStory(GetCommentsByStoryQuery query) {
		log.info(
				"CommentQueryService : getByStory : Story별 댓글 목록 조회 시작 - storyUuid={}, sort={}",
				query.storyUuid(),
				query.sort()
		);
		List<CommentQueryResult> comments = commentQueryPort.findActiveByStoryUuid(query.storyUuid());
		List<CommentThreadResult> threads = commentThreadAssembler.assemble(
				comments,
				query.sort(),
				query.viewerMemberUuid(),
				query.viewerRole()
		);
		log.debug(
				"CommentQueryService : getByStory : Story별 댓글 목록 조회 완료 - storyUuid={}, rootCount={}",
				query.storyUuid(),
				threads.size()
		);
		return threads;
	}

	@Override
	public List<ManagedCommentResult> getManagedComments(GetManagedCommentsQuery query) {
		commentManagementAuthorizer.assertCanManage(
				query.storyUuid(),
				query.requesterUuid(),
				query.requesterRole()
		);
		return commentQueryPort.findHiddenByStoryUuid(query.storyUuid());
	}
}
