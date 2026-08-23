package com.planwith.planwith_fo_comment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.port.out.StoryProjectionPort;
import com.planwith.planwith_fo_comment.domain.exception.CommentManagementForbiddenException;
import com.planwith.planwith_fo_comment.domain.exception.LoginRequiredException;
import com.planwith.planwith_fo_comment.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentManagementAuthorizer {

	private final StoryProjectionPort storyProjectionPort;

	public void assertCanManage(UUID storyUuid, UUID requesterUuid, MemberRole requesterRole) {
		if (requesterUuid == null) {
			throw new LoginRequiredException();
		}

		StoryProjection story = storyProjectionPort.findByStoryUuid(storyUuid)
				.orElseThrow(() -> new StoryNotFoundException(storyUuid));
		if (requesterUuid.equals(story.getOwnerMemberUuid()) || requesterRole.isAdmin()) {
			return;
		}
		throw new CommentManagementForbiddenException(storyUuid);
	}
}
