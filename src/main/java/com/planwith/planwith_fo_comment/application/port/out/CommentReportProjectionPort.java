package com.planwith.planwith_fo_comment.application.port.out;

import java.util.UUID;

public interface CommentReportProjectionPort {

	boolean existsByReportUuid(UUID reportUuid);

	void save(UUID reportUuid, UUID commentUuid);
}
