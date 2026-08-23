package com.planwith.planwith_fo_comment.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.query.CommentMemberResult;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.domain.comment.CommentSort;

@Component
public class CommentThreadAssembler {

	public List<CommentThreadResult> assemble(
			List<CommentQueryResult> comments,
			CommentSort sort,
			UUID viewerMemberUuid
	) {
		CommentSort resolvedSort = sort == null ? CommentSort.LATEST : sort;
		Comparator<CommentQueryResult> rootComparator = rootComparator(resolvedSort);
		Map<UUID, List<CommentQueryResult>> repliesByParent = comments.stream()
				.filter(comment -> comment.parentCommentUuid() != null)
				.collect(Collectors.groupingBy(CommentQueryResult::parentCommentUuid));

		return comments.stream()
				.filter(comment -> comment.parentCommentUuid() == null)
				.sorted(rootComparator)
				.map(root -> toThread(
						root,
						repliesByParent.getOrDefault(root.commentUuid(), List.of()),
						viewerMemberUuid
				))
				.toList();
	}

	private CommentThreadResult toThread(
			CommentQueryResult comment,
			List<CommentQueryResult> replies,
			UUID viewerMemberUuid
	) {
		List<CommentThreadResult> nestedReplies = replies.stream()
				.sorted(Comparator.comparing(CommentQueryResult::createdAt))
				.map(reply -> toThread(reply, List.of(), viewerMemberUuid))
				.toList();
		boolean owned = viewerMemberUuid != null && viewerMemberUuid.equals(comment.memberUuid());
		return new CommentThreadResult(
				comment.commentUuid(),
				comment.parentCommentUuid(),
				new CommentMemberResult(
						comment.memberUuid(),
						comment.nickname(),
						comment.profileImage()
				),
				comment.commentContent(),
				comment.likeCount(),
				comment.createdAt(),
				comment.updatedAt(),
				isUpdated(comment),
				owned,
				owned,
				nestedReplies
		);
	}

	private boolean isUpdated(CommentQueryResult comment) {
		return comment.createdAt() != null
				&& comment.updatedAt() != null
				&& comment.updatedAt().isAfter(comment.createdAt());
	}

	private Comparator<CommentQueryResult> rootComparator(CommentSort sort) {
		if (sort == CommentSort.LIKE) {
			return Comparator.comparingLong(CommentQueryResult::likeCount).reversed()
					.thenComparing(CommentQueryResult::createdAt, Comparator.reverseOrder());
		}
		return Comparator.comparing(CommentQueryResult::createdAt, Comparator.reverseOrder());
	}
}
