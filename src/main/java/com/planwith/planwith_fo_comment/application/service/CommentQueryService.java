package com.planwith.planwith_fo_comment.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.port.in.GetCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentsByStoryUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentQueryPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentUseCase, GetCommentsByStoryUseCase {

	private final CommentQueryPort commentQueryPort;

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
	public List<CommentQueryResult> getByStory(GetCommentsByStoryQuery query) {
		log.debug(
				"CommentQueryService : getByStory : Story별 댓글 목록 조회 - storyUuid={}",
				query.storyUuid()
		);
		return commentQueryPort.findActiveByStoryUuid(query.storyUuid());
	}
}
