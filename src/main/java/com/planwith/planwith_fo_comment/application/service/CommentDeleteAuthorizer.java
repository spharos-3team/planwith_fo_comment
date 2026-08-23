package com.planwith.planwith_fo_comment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.domain.comment.CommentDeletePolicy;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.exception.CommentDeleteForbiddenException;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;
import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommentDeleteAuthorizer {

	public void assertCanDelete(
			StoryComment comment,
			UUID requesterUuid,
			StoryProjection story,
			MemberRole role
	) {
		UUID storyOwnerUuid = story == null ? null : story.getOwnerMemberUuid();
		if (CommentDeletePolicy.canDelete(comment.getMemberUuid(), storyOwnerUuid, requesterUuid, role)) {
			log.debug(
					"CommentDeleteAuthorizer : assertCanDelete : 댓글 삭제 권한 확인 완료 - commentUuid={}, memberUuid={}",
					comment.getCommentUuid(),
					requesterUuid
			);
			return;
		}
		log.warn(
				"CommentDeleteAuthorizer : assertCanDelete : 댓글 삭제 권한 없음 - commentUuid={}, memberUuid={}",
				comment.getCommentUuid(),
				requesterUuid
		);
		throw new CommentDeleteForbiddenException(comment.getCommentUuid());
	}
}
