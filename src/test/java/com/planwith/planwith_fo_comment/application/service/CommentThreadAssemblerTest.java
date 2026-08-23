package com.planwith.planwith_fo_comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.domain.comment.CommentSort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

@SuppressWarnings("null")
class CommentThreadAssemblerTest {

	private final CommentThreadAssembler assembler = new CommentThreadAssembler();

	@Test
	void assembleSortsRootsByLatestAndNestsRepliesInCreatedOrder() {
		Instant now = Instant.parse("2026-08-23T12:00:00Z");
		UUID olderRootUuid = UUID.randomUUID();
		UUID newerRootUuid = UUID.randomUUID();
		UUID firstReplyUuid = UUID.randomUUID();
		UUID secondReplyUuid = UUID.randomUUID();
		UUID writerUuid = UUID.randomUUID();

		List<CommentThreadResult> threads = assembler.assemble(
				List.of(
						result(olderRootUuid, null, writerUuid, "이전 댓글", 10, now.minusSeconds(20), now.minusSeconds(20)),
						result(newerRootUuid, null, writerUuid, "최신 댓글", 0, now.minusSeconds(5), now.minusSeconds(5)),
						result(secondReplyUuid, olderRootUuid, writerUuid, "두번째 대댓글", 0, now.minusSeconds(8), now.minusSeconds(8)),
						result(firstReplyUuid, olderRootUuid, writerUuid, "첫번째 대댓글", 0, now.minusSeconds(15), now.minusSeconds(15))
				),
				CommentSort.LATEST,
				writerUuid
		);

		assertThat(threads).extracting(CommentThreadResult::commentUuid)
				.containsExactly(newerRootUuid, olderRootUuid);
		assertThat(threads.get(1).replies()).extracting(CommentThreadResult::commentUuid)
				.containsExactly(firstReplyUuid, secondReplyUuid);
		assertThat(threads.get(0).canEdit()).isTrue();
		assertThat(threads.get(0).canDelete()).isTrue();
		assertThat(threads.get(0).isUpdated()).isFalse();
		assertThat(threads.get(0).member().memberUuid()).isEqualTo(writerUuid);
	}

	@Test
	void assembleSortsRootsByLikeCountThenLatest() {
		Instant now = Instant.parse("2026-08-23T12:00:00Z");
		UUID lowLikeUuid = UUID.randomUUID();
		UUID highLikeUuid = UUID.randomUUID();
		UUID sameLikeNewerUuid = UUID.randomUUID();

		List<CommentThreadResult> threads = assembler.assemble(
				List.of(
						result(lowLikeUuid, null, UUID.randomUUID(), "적음", 1, now.minusSeconds(1), now.minusSeconds(1)),
						result(highLikeUuid, null, UUID.randomUUID(), "많음", 5, now.minusSeconds(30), now.minusSeconds(30)),
						result(sameLikeNewerUuid, null, UUID.randomUUID(), "같음 최신", 5, now.minusSeconds(10), now.minusSeconds(10))
				),
				CommentSort.LIKE,
				null
		);

		assertThat(threads).extracting(CommentThreadResult::commentUuid)
				.containsExactly(sameLikeNewerUuid, highLikeUuid, lowLikeUuid);
		assertThat(threads.get(0).canEdit()).isFalse();
		assertThat(threads.get(0).canDelete()).isFalse();
	}

	@Test
	void assembleMarksUpdatedAndOmitsOrphanReplies() {
		Instant createdAt = Instant.parse("2026-08-23T12:00:00Z");
		UUID rootUuid = UUID.randomUUID();
		UUID orphanParentUuid = UUID.randomUUID();

		List<CommentThreadResult> threads = assembler.assemble(
				List.of(
						result(rootUuid, null, UUID.randomUUID(), "수정됨", 0, createdAt, createdAt.plusSeconds(3)),
						result(UUID.randomUUID(), orphanParentUuid, UUID.randomUUID(), "고아 대댓글", 0, createdAt, createdAt)
				),
				CommentSort.LATEST,
				null
		);

		assertThat(threads).hasSize(1);
		assertThat(threads.get(0).isUpdated()).isTrue();
		assertThat(threads.get(0).replies()).isEmpty();
	}

	@Test
	void assembleKeepsDeletedParentWithRepliesAndGrantsStoryOwnerDelete() {
		Instant now = Instant.parse("2026-08-23T12:00:00Z");
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID rootUuid = UUID.randomUUID();
		UUID replyUuid = UUID.randomUUID();

		List<CommentThreadResult> threads = assembler.assemble(
				List.of(
						result(rootUuid, null, authorUuid, storyOwnerUuid, "원본", 0, now, now, true),
						result(replyUuid, rootUuid, authorUuid, storyOwnerUuid, "대댓글", 0, now.plusSeconds(1), now.plusSeconds(1), false)
				),
				CommentSort.LATEST,
				storyOwnerUuid,
				MemberRole.USER
		);

		assertThat(threads).hasSize(1);
		assertThat(threads.get(0).deleted()).isTrue();
		assertThat(threads.get(0).commentContent()).isEqualTo(StoryComment.DELETED_DISPLAY_CONTENT);
		assertThat(threads.get(0).canEdit()).isFalse();
		assertThat(threads.get(0).canDelete()).isFalse();
		assertThat(threads.get(0).replies()).extracting(CommentThreadResult::commentUuid)
				.containsExactly(replyUuid);
		assertThat(threads.get(0).replies().get(0).canDelete()).isTrue();
		assertThat(threads.get(0).replies().get(0).canEdit()).isFalse();
	}

	private CommentQueryResult result(
			UUID commentUuid,
			UUID parentCommentUuid,
			UUID memberUuid,
			String content,
			long likeCount,
			Instant createdAt,
			Instant updatedAt
	) {
		return result(commentUuid, parentCommentUuid, memberUuid, UUID.randomUUID(), content, likeCount, createdAt, updatedAt, false);
	}

	private CommentQueryResult result(
			UUID commentUuid,
			UUID parentCommentUuid,
			UUID memberUuid,
			UUID storyOwnerUuid,
			String content,
			long likeCount,
			Instant createdAt,
			Instant updatedAt,
			boolean deleted
	) {
		return new CommentQueryResult(
				commentUuid,
				UUID.randomUUID(),
				memberUuid,
				parentCommentUuid,
				"닉네임",
				"https://image.example/profile.png",
				"ACTIVE",
				content,
				likeCount,
				0L,
				storyOwnerUuid,
				true,
				"ACTIVE",
				createdAt,
				updatedAt,
				deleted
		);
	}
}
