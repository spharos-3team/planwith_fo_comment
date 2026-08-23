package com.planwith.planwith_fo_comment.application.port.out;

import java.util.UUID;

public interface CommentLikeProjectionPort {

	boolean existsByLikeUuid(UUID likeUuid);

	void save(UUID likeUuid, UUID commentUuid, UUID memberUuid);

	boolean deleteByLikeUuid(UUID likeUuid);
}
