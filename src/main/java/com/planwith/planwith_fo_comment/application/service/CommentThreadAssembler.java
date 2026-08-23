package com.planwith.planwith_fo_comment.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.query.CommentMemberResult;
import com.planwith.planwith_fo_comment.application.query.CommentPermissionResult;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.domain.comment.CommentSort;
import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentThreadAssembler {

	private final CommentPermissionResolver commentPermissionResolver;

	public List<CommentThreadResult> assemble(
			List<CommentQueryResult> comments,
			CommentSort sort,
			UUID viewerMemberUuid
	) {
		return assemble(comments, sort, viewerMemberUuid, MemberRole.USER);
	}

	public List<CommentThreadResult> assemble(
			List<CommentQueryResult> comments,
			CommentSort sort,
			UUID viewerMemberUuid,
			MemberRole viewerRole
	) {
		CommentSort resolvedSort = sort == null ? CommentSort.LATEST : sort;
		MemberRole resolvedRole = viewerRole == null ? MemberRole.USER : viewerRole;
		Comparator<CommentQueryResult> rootComparator = rootComparator(resolvedSort);
		Map<UUID, List<CommentQueryResult>> repliesByParent = groupVisibleRepliesByParent(comments);

		return comments.stream()
				.filter(comment -> comment.parentCommentUuid() == null)
				.sorted(rootComparator)
				.map(root -> toThread(
						root,
						repliesByParent.getOrDefault(root.commentUuid(), List.of()),
						viewerMemberUuid,
						resolvedRole
				))
				.toList();
	}

	private Map<UUID, List<CommentQueryResult>> groupVisibleRepliesByParent(List<CommentQueryResult> comments) {
		Map<UUID, List<CommentQueryResult>> repliesByParent = new HashMap<>();
		for (CommentQueryResult comment : comments) {
			UUID parentCommentUuid = comment.parentCommentUuid();
			if (parentCommentUuid != null && !comment.deleted()) {
				List<CommentQueryResult> replies = repliesByParent.get(parentCommentUuid);
				if (replies == null) {
					replies = new ArrayList<>();
					repliesByParent.put(parentCommentUuid, replies);
				}
				replies.add(comment);
			}
		}
		return repliesByParent;
	}

	@SuppressWarnings("null")
	private CommentThreadResult toThread(
			CommentQueryResult comment,
			List<CommentQueryResult> replies,
			UUID viewerMemberUuid,
			MemberRole viewerRole
	) {
		List<CommentThreadResult> nestedReplies = replies.stream()
				.sorted(Comparator.comparing(CommentQueryResult::createdAt))
				.map(reply -> toThread(reply, List.of(), viewerMemberUuid, viewerRole))
				.toList();
		boolean deleted = comment.deleted();
		CommentPermissionResult permission = commentPermissionResolver.resolve(
				comment,
				viewerMemberUuid,
				viewerRole
		);
		return new CommentThreadResult(
				comment.commentUuid(),
				comment.parentCommentUuid(),
				new CommentMemberResult(
						comment.memberUuid(),
						comment.nickname(),
						comment.profileImage()
				),
				deleted ? StoryComment.DELETED_DISPLAY_CONTENT : comment.commentContent(),
				comment.likeCount(),
				comment.createdAt(),
				comment.updatedAt(),
				!deleted && isUpdated(comment),
				permission.canEdit(),
				permission.canDelete(),
				deleted,
				nestedReplies
		);
	}

	private boolean isUpdated(CommentQueryResult comment) {
		return comment.createdAt() != null
				&& comment.updatedAt() != null
				&& comment.updatedAt().isAfter(comment.createdAt());
	}

	@SuppressWarnings("null")
	private Comparator<CommentQueryResult> rootComparator(CommentSort sort) {
		if (sort == CommentSort.LIKE) {
			return Comparator.comparingLong(CommentQueryResult::likeCount).reversed()
					.thenComparing(CommentQueryResult::createdAt, Comparator.reverseOrder());
		}
		return Comparator.comparing(CommentQueryResult::createdAt, Comparator.reverseOrder());
	}
}
