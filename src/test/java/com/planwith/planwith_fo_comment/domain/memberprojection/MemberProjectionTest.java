package com.planwith.planwith_fo_comment.domain.memberprojection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MemberProjectionTest {

	@Test
	void ignoreStaleMemberEvent() {
		UUID memberUuid = UUID.randomUUID();
		MemberProjection projection = MemberProjection.create(memberUuid, "최신", "new.png", MemberStatus.ACTIVE);

		assertThat(projection.apply("최신", "new.png", MemberStatus.ACTIVE, 4L)).isTrue();
		assertThat(projection.apply("이전", "old.png", MemberStatus.SUSPENDED, 2L)).isFalse();
		assertThat(projection.getNickname()).isEqualTo("최신");
		assertThat(projection.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
		assertThat(projection.getSourceVersion()).isEqualTo(4L);
	}

	@Test
	void truncateNicknameToTwentyCharacters() {
		MemberProjection projection = MemberProjection.create(
				UUID.randomUUID(),
				"가나다라마바사아자차카타파하ABCDEFG",
				null,
				MemberStatus.ACTIVE
		);

		assertThat(projection.getNickname()).hasSize(20);
	}
}
