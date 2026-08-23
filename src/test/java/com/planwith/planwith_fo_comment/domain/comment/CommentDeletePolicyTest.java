package com.planwith.planwith_fo_comment.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

class CommentDeletePolicyTest {

	@Test
	void allowsAuthorStoryOwnerAndAdminOnly() {
		UUID authorUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();

		assertThat(CommentDeletePolicy.canDelete(authorUuid, storyOwnerUuid, authorUuid, MemberRole.USER)).isTrue();
		assertThat(CommentDeletePolicy.canDelete(authorUuid, storyOwnerUuid, storyOwnerUuid, MemberRole.USER)).isTrue();
		assertThat(CommentDeletePolicy.canDelete(authorUuid, storyOwnerUuid, otherUuid, MemberRole.ADMIN)).isTrue();
		assertThat(CommentDeletePolicy.canDelete(authorUuid, storyOwnerUuid, otherUuid, MemberRole.USER)).isFalse();
		assertThat(CommentDeletePolicy.canDelete(authorUuid, storyOwnerUuid, null, MemberRole.ADMIN)).isFalse();
	}
}
