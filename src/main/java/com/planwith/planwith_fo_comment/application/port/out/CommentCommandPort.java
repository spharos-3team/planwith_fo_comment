package com.planwith.planwith_fo_comment.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.Comment;

public interface CommentCommandPort {

	void save(Comment comment);

	Optional<Comment> findByUuid(UUID commentUuid);
}
