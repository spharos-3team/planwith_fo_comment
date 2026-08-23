package com.planwith.planwith_fo_comment.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.StoryComment;

public interface CommentCommandPort {

	void save(StoryComment comment);

	Optional<StoryComment> findByUuid(UUID commentUuid);
}
