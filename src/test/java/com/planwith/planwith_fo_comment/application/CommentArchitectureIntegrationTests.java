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
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.UpdateCommentCommand;
import com.planwith.planwith_fo_comment.application.port.in.CreateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.DeleteCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentsByStoryUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeCreatedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeRemovedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.in.UpdateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.out.CommentOutboxPort;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;
import com.planwith.planwith_fo_comment.domain.comment.CommentEventType;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

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
	private HandleLikeCreatedUseCase handleLikeCreatedUseCase;

	@Autowired
	private HandleLikeRemovedUseCase handleLikeRemovedUseCase;

	@Autowired
	private CommentOutboxPort commentOutboxPort;

	@Test
	void createCommentSavesOutboxInSameTransaction() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();

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

		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(storyUuid, memberUuid, "조회용 댓글")
		);

		CommentQueryResult detail = getCommentUseCase.get(new GetCommentQuery(created.commentUuid()));
		List<CommentQueryResult> list = getCommentsByStoryUseCase.getByStory(new GetCommentsByStoryQuery(storyUuid));

		assertThat(detail.nickname()).isEqualTo("닉네임");
		assertThat(detail.profileImage()).isEqualTo("https://image.example/profile.png");
		assertThat(detail.memberStatus()).isEqualTo("ACTIVE");
		assertThat(list).extracting(CommentQueryResult::commentUuid).containsExactly(created.commentUuid());
	}

	@Test
	void updateAndDeleteFollowCommandRules() {
		UUID memberUuid = UUID.randomUUID();
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(UUID.randomUUID(), memberUuid, "원본")
		);

		assertThatThrownBy(() -> updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), UUID.randomUUID(), "권한없음")
		)).isInstanceOf(CommentOwnerMismatchException.class);

		CommentQueryResult updated = updateCommentUseCase.update(
				new UpdateCommentCommand(created.commentUuid(), memberUuid, "수정됨")
		);
		assertThat(updated.content()).isEqualTo("수정됨");

		deleteCommentUseCase.delete(new DeleteCommentCommand(created.commentUuid(), memberUuid));
		assertThatThrownBy(() -> getCommentUseCase.get(new GetCommentQuery(created.commentUuid())))
				.isInstanceOf(CommentNotFoundException.class);
	}

	@Test
	void likeEventsUpdateLocalLikeProjection() {
		UUID memberUuid = UUID.randomUUID();
		CommentQueryResult created = createCommentUseCase.create(
				new CreateCommentCommand(UUID.randomUUID(), memberUuid, "좋아요 대상")
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
}
