package com.planwith.planwith_fo_comment.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.ManagedCommentResult;

public interface CommentQueryPort {

	Optional<CommentQueryResult> findActiveByUuid(UUID commentUuid);

	List<CommentQueryResult> findActiveByStoryUuid(UUID storyUuid);

	List<ManagedCommentResult> findHiddenByStoryUuid(UUID storyUuid);
}
