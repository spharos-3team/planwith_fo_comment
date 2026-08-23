package com.planwith.planwith_fo_comment.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_comment.application.query.GetManagedCommentsQuery;
import com.planwith.planwith_fo_comment.application.query.ManagedCommentResult;

public interface GetManagedCommentsUseCase {

	List<ManagedCommentResult> getManagedComments(GetManagedCommentsQuery query);
}
