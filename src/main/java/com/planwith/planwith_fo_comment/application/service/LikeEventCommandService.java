package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentLikedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentUnlikedUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentLikeProjectionPort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeEventCommandService implements HandleCommentLikedUseCase, HandleCommentUnlikedUseCase {
	private static final String LIKE_TARGET_TYPE = "LIKE";

	private final CommentCommandPort commentCommandPort;
	private final CommentLikeProjectionPort commentLikeProjectionPort;
	private final ProcessedCommentEventService processedCommentEventService;

	@Override
	@Transactional
	public void handleLiked(HandleLikeCommand command) {
		StoryComment comment = findActiveCommentForUpdate(command);
		if (comment == null || shouldIgnore(command)) {
			return;
		}
		if (commentLikeProjectionPort.existsByLikeUuid(command.likeUuid())) {
			record(command);
			return;
		}

		comment.increaseLikeCount();
		commentCommandPort.save(comment);
		commentLikeProjectionPort.save(command.likeUuid(), command.commentUuid(), command.memberUuid());
		record(command);
		log.info("CommentLiked applied - eventUuid={}, likeUuid={}, commentUuid={}",
				eventUuid(command), command.likeUuid(), command.commentUuid());
	}

	@Override
	@Transactional
	public void handleUnliked(HandleLikeCommand command) {
		StoryComment comment = findActiveCommentForUpdate(command);
		if (comment == null || shouldIgnore(command)) {
			return;
		}

		boolean removed = commentLikeProjectionPort.deleteByLikeUuid(command.likeUuid());
		if (removed) {
			comment.decreaseLikeCount();
			commentCommandPort.save(comment);
		}
		record(command);
		log.info("CommentUnliked applied - eventUuid={}, likeUuid={}, commentUuid={}",
				eventUuid(command), command.likeUuid(), command.commentUuid());
	}

	private StoryComment findActiveCommentForUpdate(HandleLikeCommand command) {
		StoryComment comment = commentCommandPort.findByUuidForUpdate(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn("Like event ignored because comment is unavailable - commentUuid={}", command.commentUuid());
			return null;
		}
		return comment;
	}

	private boolean shouldIgnore(HandleLikeCommand command) {
		if (processedCommentEventService.isDuplicate(command.eventMetadata())) {
			log.warn("Duplicate like event ignored - eventUuid={}", eventUuid(command));
			return true;
		}
		if (processedCommentEventService.isOlderThanLatest(LIKE_TARGET_TYPE, command.eventMetadata())) {
			record(command);
			log.warn("Out-of-order like event ignored - eventUuid={}", eventUuid(command));
			return true;
		}
		return false;
	}

	private void record(HandleLikeCommand command) {
		processedCommentEventService.record(LIKE_TARGET_TYPE, command.eventMetadata());
	}

	private Object eventUuid(HandleLikeCommand command) {
		return command.eventMetadata() == null ? null : command.eventMetadata().eventUuid();
	}
}
