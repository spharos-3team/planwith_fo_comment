package com.planwith.planwith_fo_comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeCreatedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeRemovedUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentLikeProjectionPort;
import com.planwith.planwith_fo_comment.domain.comment.Comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeEventCommandService implements HandleLikeCreatedUseCase, HandleLikeRemovedUseCase {

	private final CommentCommandPort commentCommandPort;
	private final CommentLikeProjectionPort commentLikeProjectionPort;

	@Override
	@Transactional
	public void handleCreated(HandleLikeCommand command) {
		log.info(
				"LikeEventCommandService : handleCreated : LikeCreated 소비 시작 - likeUuid={}, commentUuid={}",
				command.likeUuid(),
				command.commentUuid()
		);

		if (commentLikeProjectionPort.existsByLikeUuid(command.likeUuid())) {
			log.warn(
					"LikeEventCommandService : handleCreated : 중복 LikeCreated 이벤트 무시 - likeUuid={}",
					command.likeUuid()
			);
			return;
		}

		Comment comment = commentCommandPort.findByUuid(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn(
					"LikeEventCommandService : handleCreated : 로컬 댓글이 없어 Like 반영을 건너뜀 - commentUuid={}",
					command.commentUuid()
			);
			return;
		}

		comment.increaseLikeCount();
		commentCommandPort.save(comment);
		commentLikeProjectionPort.save(command.likeUuid(), command.commentUuid(), command.memberUuid());

		log.info(
				"LikeEventCommandService : handleCreated : LikeCreated 반영 완료 - commentUuid={}, likeCount={}",
				comment.getCommentUuid(),
				comment.getLikeCount()
		);
	}

	@Override
	@Transactional
	public void handleRemoved(HandleLikeCommand command) {
		log.info(
				"LikeEventCommandService : handleRemoved : LikeRemoved 소비 시작 - likeUuid={}, commentUuid={}",
				command.likeUuid(),
				command.commentUuid()
		);

		boolean removed = commentLikeProjectionPort.deleteByLikeUuid(command.likeUuid());
		if (!removed) {
			log.warn(
					"LikeEventCommandService : handleRemoved : 로컬 Like Projection이 없어 무시 - likeUuid={}",
					command.likeUuid()
			);
			return;
		}

		Comment comment = commentCommandPort.findByUuid(command.commentUuid()).orElse(null);
		if (comment == null || !comment.isActive()) {
			log.warn(
					"LikeEventCommandService : handleRemoved : 로컬 댓글이 없어 카운트 감소를 건너뜀 - commentUuid={}",
					command.commentUuid()
			);
			return;
		}

		comment.decreaseLikeCount();
		commentCommandPort.save(comment);

		log.info(
				"LikeEventCommandService : handleRemoved : LikeRemoved 반영 완료 - commentUuid={}, likeCount={}",
				comment.getCommentUuid(),
				comment.getLikeCount()
		);
	}
}
