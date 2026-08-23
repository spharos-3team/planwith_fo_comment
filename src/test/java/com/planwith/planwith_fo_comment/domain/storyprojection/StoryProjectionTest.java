package com.planwith.planwith_fo_comment.domain.storyprojection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.domain.exception.CommentNotAllowedException;
import com.planwith.planwith_fo_comment.domain.exception.StoryDeletedException;

class StoryProjectionTest {

	@Test
	void ignoreStaleStoryEventAndApplyNewerVersion() {
		UUID storyUuid = UUID.randomUUID();
		UUID firstOwner = UUID.randomUUID();
		UUID secondOwner = UUID.randomUUID();
		StoryProjection projection = StoryProjection.create(storyUuid, firstOwner, true, StoryStatus.ACTIVE);

		assertThat(projection.apply(secondOwner, false, StoryStatus.ACTIVE, 3L)).isTrue();
		assertThat(projection.apply(firstOwner, true, StoryStatus.ACTIVE, 2L)).isFalse();
		assertThat(projection.getOwnerMemberUuid()).isEqualTo(secondOwner);
		assertThat(projection.isCommentEnabled()).isFalse();
		assertThat(projection.getSourceVersion()).isEqualTo(3L);
	}

	@Test
	void markDeletedDisablesComment() {
		StoryProjection projection = StoryProjection.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				true,
				StoryStatus.ACTIVE
		);

		assertThat(projection.markDeleted(1L)).isTrue();
		assertThat(projection.getStoryStatus()).isEqualTo(StoryStatus.DELETED);
		assertThat(projection.isCommentEnabled()).isFalse();
		assertThat(projection.markDeleted(1L)).isFalse();
	}

	@Test
	void assertCommentWritableRejectsDeletedOrDisabledStory() {
		StoryProjection writable = StoryProjection.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				true,
				StoryStatus.ACTIVE
		);
		assertThatCode(writable::assertCommentWritable).doesNotThrowAnyException();

		StoryProjection disabled = StoryProjection.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				false,
				StoryStatus.ACTIVE
		);
		assertThatThrownBy(disabled::assertCommentWritable)
				.isInstanceOf(CommentNotAllowedException.class);

		writable.markDeleted(1L);
		assertThatThrownBy(writable::assertCommentWritable)
				.isInstanceOf(StoryDeletedException.class);
	}
}
