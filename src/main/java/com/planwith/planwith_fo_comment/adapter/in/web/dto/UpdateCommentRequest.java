package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record UpdateCommentRequest(
		@Schema(description = "댓글 내용", example = "내용을 수정합니다.")
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(max = 2000, message = "댓글 내용은 2000자를 초과할 수 없습니다.")
		String content
) {
}
