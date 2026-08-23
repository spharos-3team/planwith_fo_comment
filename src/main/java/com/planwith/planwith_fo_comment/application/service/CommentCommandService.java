package com.planwith.planwith_fo_comment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.CreateCommentCommand;
import com.planwith.planwith_fo_comment.application.command.DeleteCommentCommand;
import com.planwith.planwith_fo_comment.application.command.UpdateCommentCommand;
import com.planwith.planwith_fo_comment.application.port.in.CreateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.DeleteCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.UpdateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentOutboxPort;
import com.planwith.planwith_fo_comment.application.port.out.MemberProjectionPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCommandService implements CreateCommentUseCase, UpdateCommentUseCase, DeleteCommentUseCase {

	private final CommentCommandPort commentCommandPort;
	private final CommentOutboxPort commentOutboxPort;
	private final MemberProjectionPort memberProjectionPort;

	@Override
	@Transactional
	public CommentQueryResult create(CreateCommentCommand command) {
		log.info("CommentCommandService : create : 댓글 생성 비즈니스 로직 시작");
		log.debug(
				"CommentCommandService : create : 댓글 생성 요청 데이터 확인 - storyUuid={}, memberUuid={}, parentCommentUuid={}",
				command.storyUuid(),
				command.memberUuid(),
				command.parentCommentUuid()
		);

		StoryComment comment = createComment(command);
		commentCommandPort.save(comment);
		commentOutboxPort.saveCommentCreated(comment);

		log.info(
				"CommentCommandService : create : 댓글 생성 완료 - commentUuid={}, storyUuid={}, parentCommentUuid={}",
				comment.getCommentUuid(),
				comment.getStoryUuid(),
				comment.getParentCommentUuid()
		);
		return toQueryResult(comment);
	}

	@Override
	@Transactional
	public CommentQueryResult update(UpdateCommentCommand command) {
		log.info("CommentCommandService : update : 댓글 수정 비즈니스 로직 시작");

		StoryComment comment = findActiveComment(command.commentUuid());
		comment.updateContent(command.memberUuid(), command.content());
		commentCommandPort.save(comment);
		commentOutboxPort.saveCommentUpdated(comment);

		log.info(
				"CommentCommandService : update : 댓글 수정 완료 - commentUuid={}",
				comment.getCommentUuid()
		);
		return toQueryResult(comment);
	}

	@Override
	@Transactional
	public void delete(DeleteCommentCommand command) {
		log.info("CommentCommandService : delete : 댓글 삭제 비즈니스 로직 시작");

		StoryComment comment = findActiveComment(command.commentUuid());
		comment.delete(command.memberUuid());
		commentCommandPort.save(comment);
		commentOutboxPort.saveCommentDeleted(comment);

		log.info(
				"CommentCommandService : delete : 댓글 삭제 완료 - commentUuid={}",
				comment.getCommentUuid()
		);
	}

	private StoryComment createComment(CreateCommentCommand command) {
		if (command.parentCommentUuid() == null) {
			return StoryComment.createRoot(command.storyUuid(), command.memberUuid(), command.content());
		}

		StoryComment parent = findActiveComment(command.parentCommentUuid());
		if (!parent.getStoryUuid().equals(command.storyUuid())) {
			throw new CommentNotFoundException(command.parentCommentUuid());
		}
		return StoryComment.createReply(parent, command.memberUuid(), command.content());
	}

	private StoryComment findActiveComment(UUID commentUuid) {
		StoryComment comment = commentCommandPort.findByUuid(commentUuid)
				.orElseThrow(() -> new CommentNotFoundException(commentUuid));
		if (!comment.isActive()) {
			throw new CommentNotFoundException(commentUuid);
		}
		return comment;
	}

	private CommentQueryResult toQueryResult(StoryComment comment) {
		MemberProjection projection = memberProjectionPort.findByMemberUuid(comment.getMemberUuid())
				.orElse(null);
		return new CommentQueryResult(
				comment.getCommentUuid(),
				comment.getStoryUuid(),
				comment.getMemberUuid(),
				comment.getParentCommentUuid(),
				projection == null ? null : projection.getNickname(),
				projection == null ? null : projection.getProfileImage(),
				projection == null || projection.getMemberStatus() == null
						? null
						: projection.getMemberStatus().name(),
				comment.getCommentContent(),
				comment.getCommentLikeCount(),
				comment.getCreatedAt(),
				comment.getUpdatedAt()
		);
	}
}
