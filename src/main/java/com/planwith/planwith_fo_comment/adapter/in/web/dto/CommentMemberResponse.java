package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.CommentMemberResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 작성자. comment_member_projection에서 채운다.")
public record CommentMemberResponse(
		UUID memberUuid,
		String nickname,
		String profileImage
) {

	public static CommentMemberResponse from(CommentMemberResult result) {
		if (result == null) {
			return null;
		}
		return new CommentMemberResponse(
				result.memberUuid(),
				result.nickname(),
				result.profileImage()
		);
	}
}
