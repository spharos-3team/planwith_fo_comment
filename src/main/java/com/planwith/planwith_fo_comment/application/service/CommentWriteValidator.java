package com.planwith.planwith_fo_comment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.StoryProjectionPort;
import com.planwith.planwith_fo_comment.domain.exception.LoginRequiredException;
import com.planwith.planwith_fo_comment.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentWriteValidator {

	private final StoryProjectionPort storyProjectionPort;

	public void assertLoggedIn(UUID memberUuid) {
		if (memberUuid == null) {
			log.warn("CommentWriteValidator : assertLoggedIn : 비회원 댓글 변경 요청 차단");
			throw new LoginRequiredException();
		}
	}

	public void assertCanCreate(UUID memberUuid, UUID storyUuid) {
		assertLoggedIn(memberUuid);
		StoryProjection story = storyProjectionPort.findByStoryUuid(storyUuid)
				.orElseThrow(() -> {
					log.warn(
							"CommentWriteValidator : assertCanCreate : Story Projection 없음 - storyUuid={}",
							storyUuid
					);
					return new StoryNotFoundException(storyUuid);
				});
		story.assertCommentWritable();
		log.debug(
				"CommentWriteValidator : assertCanCreate : 댓글 작성 검증 통과 - storyUuid={}, memberUuid={}",
				storyUuid,
				memberUuid
		);
	}

	public void assertCanMutate(UUID memberUuid) {
		assertLoggedIn(memberUuid);
	}
}
