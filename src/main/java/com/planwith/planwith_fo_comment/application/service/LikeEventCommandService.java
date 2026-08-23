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

	private final CommentCommandPort commentCommandPort;
	private final CommentLikeProjectionPort commentLikeProjectionPort;

	@Override
	@Transactional
	public void handleLiked(HandleLikeCommand command) {
		log.info(
				"LikeEventCommandService : handleLiked : CommentLiked 소비 시작 - likeUuid={}, commentUuid={}",
				command.likeUuid(),
				command.commentUuid()
		);

		if (commentLikeProjectionPort.existsByLikeUuid(command.likeUuid())) {
			log.warn(
					"LikeEventCommandService : handleLiked : 중복 CommentLiked 이벤트 무시 - likeUuid={}",
					command.likeUuid()
			);
			return;
		}

		StoryComment comment = commentCommandPort.findByUuid(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn(
					"LikeEventCommandService : handleLiked : 로컬 댓글이 없어 like_count 반영을 건너뜀 - commentUuid={}",
					command.commentUuid()
			);
			return;
		}

		comment.increaseLikeCount();
		commentCommandPort.save(comment);
		commentLikeProjectionPort.save(command.likeUuid(), command.commentUuid(), command.memberUuid());

		log.info(
				"LikeEventCommandService : handleLiked : comment_like_count 증가 완료 - commentUuid={}, likeCount={}",
				comment.getCommentUuid(),
				comment.getCommentLikeCount()
		);
	}

	@Override
	@Transactional
	public void handleUnliked(HandleLikeCommand command) {
		log.info(
				"LikeEventCommandService : handleUnliked : CommentUnliked 소비 시작 - likeUuid={}, commentUuid={}",
				command.likeUuid(),
				command.commentUuid()
		);

		boolean removed = commentLikeProjectionPort.deleteByLikeUuid(command.likeUuid());
		if (!removed) {
			log.warn(
					"LikeEventCommandService : handleUnliked : 처리된 Like가 없어 comment_like_count 감소를 건너뜀 - likeUuid={}",
					command.likeUuid()
			);
			return;
		}

		StoryComment comment = commentCommandPort.findByUuid(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn(
					"LikeEventCommandService : handleUnliked : 로컬 댓글이 없어 comment_like_count 감소를 건너뜀 - commentUuid={}",
					command.commentUuid()
			);
			return;
		}

		comment.decreaseLikeCount();
		commentCommandPort.save(comment);

		log.info(
				"LikeEventCommandService : handleUnliked : comment_like_count 감소 완료 - commentUuid={}, likeCount={}",
				comment.getCommentUuid(),
				comment.getCommentLikeCount()
		);
	}
}
