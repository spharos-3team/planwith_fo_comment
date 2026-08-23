package com.planwith.planwith_fo_comment.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;

class CommentTest {

	@Test
	void createCommentStartsActiveWithZeroLikes() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();

		Comment comment = Comment.create(storyUuid, memberUuid, "첫 댓글");

		assertThat(comment.getStoryUuid()).isEqualTo(storyUuid);
		assertThat(comment.getMemberUuid()).isEqualTo(memberUuid);
		assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
		assertThat(comment.getLikeCount()).isZero();
		assertThat(comment.getCommentUuid()).isNotNull();
	}

	@Test
	void updateAndDeleteAreAllowedOnlyForOwner() {
		UUID ownerUuid = UUID.randomUUID();
		Comment comment = Comment.create(UUID.randomUUID(), ownerUuid, "원본");

		comment.updateContent(ownerUuid, "수정");
		assertThat(comment.getContent()).isEqualTo("수정");

		assertThatThrownBy(() -> comment.delete(UUID.randomUUID()))
				.isInstanceOf(CommentOwnerMismatchException.class);

		comment.delete(ownerUuid);
		assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
		assertThatThrownBy(() -> comment.updateContent(ownerUuid, "다시"))
				.isInstanceOf(CommentAlreadyDeletedException.class);
	}

	@Test
	void likeCountNeverGoesBelowZero() {
		Comment comment = Comment.create(UUID.randomUUID(), UUID.randomUUID(), "좋아요");

		comment.increaseLikeCount();
		comment.increaseLikeCount();
		comment.decreaseLikeCount();
		comment.decreaseLikeCount();
		comment.decreaseLikeCount();

		assertThat(comment.getLikeCount()).isZero();
	}
}
