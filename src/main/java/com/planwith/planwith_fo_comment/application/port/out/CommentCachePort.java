package com.planwith.planwith_fo_comment.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;

/**
 * 향후 인기 Story 첫 페이지, Hot 댓글, 인기 댓글 순위 캐시용 확장 포트.
 * 초기 단계에서는 구현체와 Redis 의존성을 추가하지 않는다.
 */
public interface CommentCachePort {

	Optional<List<CommentQueryResult>> findHotComments(UUID storyUuid);

	void evictStoryComments(UUID storyUuid);
}
