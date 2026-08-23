package com.planwith.planwith_fo_comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.application.query.CommentPermissionResult;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

class CommentPermissionResolverTest {

	private final CommentPermissionResolver resolver = new CommentPermissionResolver();

	@Test
	void resolvesPermissionsByViewerRelationshipAndRole() {
		UUID authorUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID otherMemberUuid = UUID.randomUUID();
		CommentQueryResult comment = comment(authorUuid, storyOwnerUuid, false);

		assertPermission(resolver.resolve(comment, null, MemberRole.USER), false, false);
		assertPermission(resolver.resolve(comment, otherMemberUuid, MemberRole.USER), false, false);
		assertPermission(resolver.resolve(comment, authorUuid, MemberRole.USER), true, true);
		assertPermission(resolver.resolve(comment, storyOwnerUuid, MemberRole.USER), false, true);
		assertPermission(resolver.resolve(comment, otherMemberUuid, MemberRole.ADMIN), false, true);
	}

	@Test
	void deniesEditAndDeleteForDeletedComment() {
		UUID authorUuid = UUID.randomUUID();
		CommentQueryResult deletedComment = comment(authorUuid, UUID.randomUUID(), true);

		assertPermission(resolver.resolve(deletedComment, authorUuid, MemberRole.USER), false, false);
	}

	private void assertPermission(CommentPermissionResult permission, boolean canEdit, boolean canDelete) {
		assertThat(permission.canEdit()).isEqualTo(canEdit);
		assertThat(permission.canDelete()).isEqualTo(canDelete);
	}

	private CommentQueryResult comment(UUID authorUuid, UUID storyOwnerUuid, boolean deleted) {
		Instant now = Instant.now();
		return new CommentQueryResult(
				UUID.randomUUID(),
				UUID.randomUUID(),
				authorUuid,
				null,
				"nickname",
				null,
				"ACTIVE",
				"content",
				0L,
				0L,
				storyOwnerUuid,
				true,
				"ACTIVE",
				now,
				now,
				deleted
		);
	}
}
