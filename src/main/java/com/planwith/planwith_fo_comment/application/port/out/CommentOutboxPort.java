package com.planwith.planwith_fo_comment.application.port.out;

import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.StoryComment;
import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

public interface CommentOutboxPort {

	void saveCommentCreated(StoryComment comment);

	void saveCommentUpdated(StoryComment comment);

	void saveCommentDeleted(StoryComment comment);

	List<CommentOutboxEvent> findPending(int limit);

	void markPublished(UUID outboxUuid);
}
