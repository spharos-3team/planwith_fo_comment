package com.planwith.planwith_fo_comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_comment.application.command.CreateCommentCommand;
import com.planwith.planwith_fo_comment.application.command.DeleteCommentCommand;
import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.command.MarkStoryDeletedCommand;
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.UpdateCommentCommand;
import com.planwith.planwith_fo_comment.application.port.in.CreateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.DeleteCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentsByStoryUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeCreatedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeRemovedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.MarkStoryDeletedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncStoryProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.in.UpdateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentOutboxPort;
import com.planwith.planwith_fo_comment.application.port.out.CommentCommandPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;
import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;
import com.planwith.planwith_fo_comment.domain.comment.CommentSort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentDeleteForbiddenException;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotAllowedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidReplyException;
import com.planwith.planwith_fo_comment.domain.exception.LoginRequiredException;
import com.planwith.planwith_fo_comment.domain.exception.StoryDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;
import com.planwith.planwith_fo_comment.domain.outbox.OutboxStatus;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CommentArchitectureIntegrationTests {

	@Autowired
	private CreateCommentUseCase createCommentUseCase;

	@Autowired
	private UpdateCommentUseCase updateCommentUseCase;

	@Autowired
	private DeleteCommentUseCase deleteCommentUseCase;

	@Autowired
	private GetCommentUseCase getCommentUseCase;

	@Autowired
	private GetCommentsByStoryUseCase getCommentsByStoryUseCase;

	@Autowired
	private SyncMemberProjectionUseCase syncMemberProjectionUseCase;

	@Autowired
	private SyncStoryProjectionUseCase syncStoryProjectionUseCase;

	@Autowired
	private MarkStoryDeletedUseCase markStoryDeletedUseCase;

	@Autowired
	private HandleLikeCreatedUseCase handleLikeCreatedUseCase;

	@Autowired
	private HandleLikeRemovedUseCase handleLikeRemovedUseCase;

	@Autowired
	private CommentOutboxPort commentOutboxPort;

	@Autowired
	private CommentCommandPort commentCommandPort;

	@Test
	void createCommentSavesOutboxInSameTransaction() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		enableStory(storyUuid);

		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "아키텍처 댓글")
		);

		List<CommentOutboxEvent> pending = commentOutboxPort.findPending(10);
		assertThat(pending).isNotEmpty();
		assertThat(pending)
				.anyMatch(event -> event.getAggregateUuid().equals(created.commentUuid())
						&& event.getEventType() == CommentEventType.COMMENT_CREATED);
	}

	@Test
	void queryUsesLocalMemberProjectionInsteadOfMemberApi() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				memberUuid,
				"닉네임",
				"https://image.example/profile.png",
				"ACTIVE"
		));
		enableStory(storyUuid);

		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "조회용 댓글")
		);

		CommentQueryResult detail = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		List<CommentThreadResult> list = getCommentsByStoryUseCase.getByStory(new GetCommentsByStoryQuery(storyUuid));

		assertThat(detail.nickname()).isEqualTo("닉네임");
		assertThat(detail.profileImage()).isEqualTo("https://image.example/profile.png");
		assertThat(detail.memberStatus()).isEqualTo("ACTIVE");
		assertThat(detail.commentContent()).isEqualTo("조회용 댓글");
		assertThat(detail.reportCount()).isZero();
		assertThat(list).extracting(CommentThreadResult::commentUuid).containsExactly(created.commentUuid());
		assertThat(list.get(0).member().nickname()).isEqualTo("닉네임");
		assertThat(list.get(0).member().profileImage()).isEqualTo("https://image.example/profile.png");
		assertThat(list.get(0).member().memberUuid()).isEqualTo(memberUuid);
	}

	@Test
	void queryUsesLocalStoryProjectionInsteadOfStoryApi() {
		UUID storyUuid = UUID.randomUUID();
		UUID ownerMemberUuid = UUID.randomUUID();
		UUID writerUuid = UUID.randomUUID();

		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				ownerMemberUuid,
				true,
				"ACTIVE",
				1L
		));
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, writerUuid, "스토리 조인용 댓글")
		);

		CommentQueryResult detail = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(detail.storyOwnerMemberUuid()).isEqualTo(ownerMemberUuid);
		assertThat(detail.commentEnabled()).isTrue();
		assertThat(detail.storyStatus()).isEqualTo("ACTIVE");

		markStoryDeletedUseCase.markDeleted(new MarkStoryDeletedCommand(storyUuid, 2L));
		CommentQueryResult afterDelete = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(afterDelete.storyStatus()).isEqualTo("DELETED");
		assertThat(afterDelete.commentEnabled()).isFalse();
	}

	@Test
	void staleMemberProjectionEventIsIgnored() {
		UUID memberUuid = UUID.randomUUID();
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				memberUuid,
				"최신닉",
				"https://image.example/new.png",
				"ACTIVE",
				5L
		));
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				memberUuid,
				"이전닉",
				"https://image.example/old.png",
				"SUSPENDED",
				3L
		));

		UUID storyUuid = UUID.randomUUID();
		enableStory(storyUuid);
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "버전 검증")
		);
		CommentQueryResult detail = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(detail.nickname()).isEqualTo("최신닉");
		assertThat(detail.memberStatus()).isEqualTo("ACTIVE");
	}

	@Test
	void updateAndDeleteFollowCommandRules() throws InterruptedException {
		UUID memberUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID storyUuid = UUID.randomUUID();
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				storyOwnerUuid,
				true,
				"ACTIVE",
				1L
		));
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "원본")
		);

		assertThatThrownBy(() -> updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), null, "비회원")
		)).isInstanceOf(LoginRequiredException.class);
		assertThatThrownBy(() -> updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), UUID.randomUUID(), "권한없음")
		)).isInstanceOf(CommentOwnerMismatchException.class);
		assertThatThrownBy(() -> updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), storyOwnerUuid, "스토리 주인")
		)).isInstanceOf(CommentOwnerMismatchException.class);

		Thread.sleep(10);
		CommentQueryResult updated = updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), memberUuid, "수정됨")
		);
		assertThat(updated.commentContent()).isEqualTo("수정됨");
		assertThat(updated.updatedAt()).isAfter(created.createdAt());

		deleteCommentUseCase.delete(new DeleteCommentCommand(created.commentUuid(), memberUuid));
		assertThatThrownBy(() -> getCommentUseCase.get(new GetCommentQuery(created.commentUuid())))
				.isInstanceOf(CommentNotFoundException.class);
		assertThatThrownBy(() -> updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), memberUuid, "삭제 후 수정")
		)).isInstanceOf(CommentNotFoundException.class);
		assertThatThrownBy(() -> deleteCommentUseCase.delete(
				new DeleteCommentCommand(created.commentUuid(), memberUuid)
		)).isInstanceOf(CommentAlreadyDeletedException.class);
	}

	@Test
	void deleteCommentAllowsAuthorStoryOwnerAndAdminAndKeepsReplies() {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		UUID adminUuid = UUID.randomUUID();
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				storyOwnerUuid,
				true,
				"ACTIVE",
				1L
		));

		CommentQueryResult authorComment = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "작성자 댓글")
		);
		CommentQueryResult reply = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, otherUuid, "대댓글", authorComment.commentUuid())
		);
		CommentQueryResult ownerTarget = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "스토리 주인이 지울 댓글")
		);
		CommentQueryResult adminTarget = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "운영자가 지울 댓글")
		);

		assertThatThrownBy(() -> deleteCommentUseCase.delete(
				new DeleteCommentCommand(authorComment.commentUuid(), null)
		)).isInstanceOf(LoginRequiredException.class);
		assertThatThrownBy(() -> deleteCommentUseCase.delete(
				new DeleteCommentCommand(authorComment.commentUuid(), otherUuid)
		)).isInstanceOf(CommentDeleteForbiddenException.class);

		deleteCommentUseCase.delete(new DeleteCommentCommand(authorComment.commentUuid(), authorUuid));
		List<CommentThreadResult> afterAuthorDelete = getCommentsByStoryUseCase.getByStory(
				new GetCommentsByStoryQuery(storyUuid, CommentSort.LATEST, storyOwnerUuid)
		);
		CommentThreadResult deletedParent = afterAuthorDelete.stream()
				.filter(thread -> thread.commentUuid().equals(authorComment.commentUuid()))
				.findFirst()
				.orElseThrow();
		assertThat(deletedParent.deleted()).isTrue();
		assertThat(deletedParent.commentContent()).isEqualTo(StoryComment.DELETED_DISPLAY_CONTENT);
		assertThat(deletedParent.replies()).extracting(CommentThreadResult::commentUuid)
				.containsExactly(reply.commentUuid());
		assertThat(deletedParent.replies().get(0).canDelete()).isTrue();

		deleteCommentUseCase.delete(new DeleteCommentCommand(ownerTarget.commentUuid(), storyOwnerUuid));
		deleteCommentUseCase.delete(new DeleteCommentCommand(
				adminTarget.commentUuid(),
				adminUuid,
				MemberRole.ADMIN
		));
		assertThatThrownBy(() -> getCommentUseCase.get(new GetCommentQuery(ownerTarget.commentUuid())))
				.isInstanceOf(CommentNotFoundException.class);
		assertThatThrownBy(() -> getCommentUseCase.get(new GetCommentQuery(adminTarget.commentUuid())))
				.isInstanceOf(CommentNotFoundException.class);
	}

	@Test
	void createCommentReturnsPersistedResultImmediatelyWithoutWaitingForKafka() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				memberUuid,
				"작성자",
				"https://image.example/writer.png",
				"ACTIVE"
		));
		enableStory(storyUuid);

		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "즉시 반환 댓글")
		);

		assertThat(created.commentUuid()).isNotNull();
		assertThat(created.storyUuid()).isEqualTo(storyUuid);
		assertThat(created.memberUuid()).isEqualTo(memberUuid);
		assertThat(created.parentCommentUuid()).isNull();
		assertThat(created.commentContent()).isEqualTo("즉시 반환 댓글");
		assertThat(created.nickname()).isEqualTo("작성자");
		assertThat(created.likeCount()).isZero();
		assertThat(created.createdAt()).isNotNull();

		CommentQueryResult persisted = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(persisted.commentUuid()).isEqualTo(created.commentUuid());
		assertThat(persisted.commentContent()).isEqualTo(created.commentContent());

		assertThat(commentOutboxPort.findPending(10))
				.anyMatch(event -> event.getAggregateUuid().equals(created.commentUuid())
						&& event.getEventType() == CommentEventType.COMMENT_CREATED
						&& event.getStatus() == OutboxStatus.PENDING);

		CommentQueryResult reply = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "즉시 반환 대댓글", created.commentUuid())
		);
		assertThat(reply.parentCommentUuid()).isEqualTo(created.commentUuid());
		assertThat(reply.commentContent()).isEqualTo("즉시 반환 대댓글");
		assertThat(getCommentUseCase.get(new GetCommentQuery(reply.commentUuid())).commentContent())
				.isEqualTo("즉시 반환 대댓글");
	}

	@Test
	void createOneLevelReplyAndRejectNestedReply() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		enableStory(storyUuid);

		CommentQueryResult parent = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "부모 댓글")
		);
		CommentQueryResult reply = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "대댓글", parent.commentUuid())
		);

		assertThat(parent.parentCommentUuid()).isNull();
		assertThat(reply.parentCommentUuid()).isEqualTo(parent.commentUuid());

		assertThatThrownBy(() -> createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "중첩 대댓글", reply.commentUuid())
		)).isInstanceOf(InvalidReplyException.class);
	}

	@Test
	void likeEventsUpdateLocalLikeProjection() {
		UUID memberUuid = UUID.randomUUID();
		UUID storyUuid = UUID.randomUUID();
		enableStory(storyUuid);
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "좋아요 대상")
		);
		UUID likeUuid = UUID.randomUUID();

		handleLikeCreatedUseCase.handleCreated(new HandleLikeCommand(likeUuid, created.commentUuid(), memberUuid));
		handleLikeCreatedUseCase.handleCreated(new HandleLikeCommand(likeUuid, created.commentUuid(), memberUuid));
		CommentQueryResult liked = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(liked.likeCount()).isEqualTo(1);

		handleLikeRemovedUseCase.handleRemoved(new HandleLikeCommand(likeUuid, created.commentUuid(), memberUuid));
		CommentQueryResult unliked = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		assertThat(unliked.likeCount()).isZero();
	}

	@Test
	void queryCommentsByStorySortsNestsRepliesAndHidesInactive() throws InterruptedException {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				authorUuid,
				"작성자",
				"https://image.example/author.png",
				"ACTIVE"
		));
		enableStory(storyUuid);

		CommentQueryResult older = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "이전 댓글")
		);
		Thread.sleep(10);
		CommentQueryResult newer = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, otherUuid, "최신 댓글")
		);
		CommentQueryResult reply = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "대댓글", older.commentUuid())
		);
		CommentQueryResult deleted = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "삭제될 댓글")
		);
		CommentQueryResult hidden = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, authorUuid, "숨김 댓글")
		);
		deleteCommentUseCase.delete(new DeleteCommentCommand(deleted.commentUuid(), authorUuid));
		commentCommandPort.findByUuid(hidden.commentUuid()).ifPresent(comment -> {
			comment.hide();
			commentCommandPort.save(comment);
		});
		handleLikeCreatedUseCase.handleCreated(new HandleLikeCommand(
				UUID.randomUUID(),
				older.commentUuid(),
				authorUuid
		));

		List<CommentThreadResult> latest = getCommentsByStoryUseCase.getByStory(
				new GetCommentsByStoryQuery(storyUuid, CommentSort.LATEST, authorUuid)
		);
		assertThat(latest).extracting(CommentThreadResult::commentUuid)
				.containsExactly(newer.commentUuid(), older.commentUuid());
		assertThat(latest.get(1).replies()).extracting(CommentThreadResult::commentUuid)
				.containsExactly(reply.commentUuid());
		assertThat(latest.get(1).commentLikeCount()).isEqualTo(1);
		assertThat(latest.get(1).canEdit()).isTrue();
		assertThat(latest.get(0).canEdit()).isFalse();
		assertThat(latest).extracting(CommentThreadResult::commentUuid)
				.doesNotContain(deleted.commentUuid(), hidden.commentUuid());

		List<CommentThreadResult> liked = getCommentsByStoryUseCase.getByStory(
				new GetCommentsByStoryQuery(storyUuid, CommentSort.LIKE, null)
		);
		assertThat(liked).extracting(CommentThreadResult::commentUuid)
				.containsExactly(older.commentUuid(), newer.commentUuid());
		assertThat(liked.get(0).canEdit()).isFalse();
		assertThat(liked.get(0).canDelete()).isFalse();
	}

	@Test
	void createCommentRequiresLoginAndWritableStory() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();

		assertThatThrownBy(() -> createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, null, "비회원")
		)).isInstanceOf(LoginRequiredException.class);

		assertThatThrownBy(() -> createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "없는 스토리")
		)).isInstanceOf(StoryNotFoundException.class);

		enableStory(storyUuid);
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				UUID.randomUUID(),
				false,
				"ACTIVE",
				2L
		));
		assertThatThrownBy(() -> createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "댓글 비허용")
		)).isInstanceOf(CommentNotAllowedException.class);

		markStoryDeletedUseCase.markDeleted(new MarkStoryDeletedCommand(storyUuid, 3L));
		assertThatThrownBy(() -> createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "삭제된 스토리")
		)).isInstanceOf(StoryDeletedException.class);
	}

	private void enableStory(UUID storyUuid) {
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				UUID.randomUUID(),
				true,
				"ACTIVE",
				1L
		));
	}
}
