package com.planwith.planwith_fo_comment.application.port.out;

import com.planwith.planwith_fo_comment.domain.outbox.CommentOutboxEvent;

public interface CommentEventPublisherPort {

	void publish(CommentOutboxEvent event);
}
