package com.planwith.planwith_fo_comment.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.storyprojection.StoryProjection;

public interface StoryProjectionPort {

	void save(StoryProjection storyProjection);

	Optional<StoryProjection> findByStoryUuid(UUID storyUuid);

	Optional<StoryProjection> findByStoryUuidForUpdate(UUID storyUuid);

	List<StoryProjection> findByStoryUuids(Collection<UUID> storyUuids);
}
