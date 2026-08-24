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
import com.planwith.planwith_fo_comment.application.port.out.StoryProjectionPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberProjection;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCommandService implements CreateCommentUseCase, UpdateCommentUseCase, DeleteCommentUseCase {

	private final CommentCommandPort commentCommandPort;
	private final CommentOutboxPort commentOutboxPort;
	private final MemberProjectionPort memberProjectionPort;
	private final StoryProjectionPort storyProjectionPort;
	private final CommentWriteValidator commentWriteValidator;
	private final CommentDeleteAuthorizer commentDeleteAuthorizer;

	@Override
	@Transactional
	public CommentQueryResult create(CreateCommentCommand command) {
		log.info("CommentCommandService : create : 댓글 작성 비즈니스 로직 시작");
		log.debug(
				"CommentCommandService : create : 댓글 작성 요청 데이터 확인 - storyUuid={}, memberUuid={}, parentCommentUuid={}",
				command.storyUuid(),
				command.memberUuid(),
				command.parentCommentUuid()
		);
		commentWriteValidator.assertCanCreate(command.memberUuid(), command.storyUuid());

		StoryComment comment = createComment(command);
		commentCommandPort.save(comment);
		commentOutboxPort.saveCommentCreated(comment);

		log.info(
				"CommentCommandService : create : 댓글 작성 완료 - commentUuid={}, storyUuid={}, parentCommentUuid={}",
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
		log.debug(
				"CommentCommandService : update : 댓글 수정 요청 데이터 확인 - commentUuid={}, memberUuid={}",
				command.commentUuid(),
				command.memberUuid()
		);
		commentWriteValidator.assertCanMutate(command.memberUuid());

		StoryComment comment = findActiveComment(command.commentUuid());
		comment.updateContent(command.memberUuid(), command.commentContent());
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
		log.debug(
				"CommentCommandService : delete : 댓글 삭제 요청 데이터 확인 - commentUuid={}, memberUuid={}",
				command.commentUuid(),
				command.memberUuid()
		);
		commentWriteValidator.assertCanMutate(command.memberUuid());

		StoryComment comment = findCommentForDelete(command.commentUuid());
		StoryProjection story = storyProjectionPort.findByStoryUuid(comment.getStoryUuid()).orElse(null);
		commentDeleteAuthorizer.assertCanDelete(comment, command.memberUuid(), story, command.requesterRole());
		comment.delete();
		commentCommandPort.save(comment);
		commentOutboxPort.saveCommentDeleted(comment);

		log.info(
				"CommentCommandService : delete : 댓글 Soft Delete 완료 - commentUuid={}",
				comment.getCommentUuid()
		);
	}

	private StoryComment createComment(CreateCommentCommand command) {
		if (command.parentCommentUuid() == null) {
			return StoryComment.createRoot(command.storyUuid(), command.memberUuid(), command.commentContent());
		}

		StoryComment replyTarget = findActiveComment(command.parentCommentUuid());
		if (!replyTarget.getStoryUuid().equals(command.storyUuid())) {
			throw new CommentNotFoundException(command.parentCommentUuid());
		}
		assertThreadRootCanReceiveReply(replyTarget, command.storyUuid());
		return StoryComment.createReply(replyTarget, command.memberUuid(), command.commentContent());
	}

	private void assertThreadRootCanReceiveReply(StoryComment replyTarget, UUID storyUuid) {
		if (replyTarget.isRoot()) {
			return;
		}

		StoryComment threadRoot = findActiveComment(replyTarget.getParentCommentUuid());
		if (!threadRoot.isRoot() || !threadRoot.getStoryUuid().equals(storyUuid)) {
			throw new CommentNotFoundException(replyTarget.getParentCommentUuid());
		}
		threadRoot.assertCanReceiveReply();
	}

	private StoryComment findActiveComment(UUID commentUuid) {
		StoryComment comment = commentCommandPort.findByUuid(commentUuid)
				.orElseThrow(() -> new CommentNotFoundException(commentUuid));
		if (!comment.isActive()) {
			throw new CommentNotFoundException(commentUuid);
		}
		return comment;
	}

	private StoryComment findCommentForDelete(UUID commentUuid) {
		StoryComment comment = commentCommandPort.findByUuid(commentUuid)
				.orElseThrow(() -> new CommentNotFoundException(commentUuid));
		if (comment.isDeleted()) {
			throw new CommentAlreadyDeletedException(commentUuid);
		}
		return comment;
	}

	private CommentQueryResult toQueryResult(StoryComment comment) {
		MemberProjection memberProjection = memberProjectionPort.findByMemberUuid(comment.getMemberUuid())
				.orElse(null);
		StoryProjection storyProjection = storyProjectionPort.findByStoryUuid(comment.getStoryUuid())
				.orElse(null);
		return new CommentQueryResult(
				comment.getCommentUuid(),
				comment.getStoryUuid(),
				comment.getMemberUuid(),
				comment.getParentCommentUuid(),
				memberProjection == null ? null : memberProjection.getNickname(),
				memberProjection == null ? null : memberProjection.getProfileImage(),
				memberProjection == null || memberProjection.getMemberStatus() == null
						? null
						: memberProjection.getMemberStatus().name(),
				comment.getCommentContent(),
				comment.getCommentLikeCount(),
				comment.getReportCount(),
				storyProjection == null ? null : storyProjection.getOwnerMemberUuid(),
				storyProjection == null ? null : storyProjection.isCommentEnabled(),
				storyProjection == null || storyProjection.getStoryStatus() == null
						? null
						: storyProjection.getStoryStatus().name(),
				comment.getCreatedAt(),
				comment.getUpdatedAt(),
				comment.isDeleted()
		);
	}
}
