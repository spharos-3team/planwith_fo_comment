package com.planwith.planwith_fo_comment.application.port.out;

import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.Comment;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

public interface CommentOutboxPort {

	void saveCommentCreated(Comment comment);

	void saveCommentUpdated(Comment comment);

	void saveCommentDeleted(Comment comment);

	List<CommentOutboxEvent> findPending(int limit);

	void markPublished(UUID outboxUuid);
}
