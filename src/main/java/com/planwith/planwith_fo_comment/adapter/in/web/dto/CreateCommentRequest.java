package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 요청")
public record CreateCommentRequest(
		@Schema(description = "Story UUID", example = "11111111-1111-1111-1111-111111111111")
		@NotNull(message = "storyUuid는 필수입니다.")
		UUID storyUuid,

		@Schema(description = "댓글 내용", example = "좋은 일정이에요.")
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(max = 2000, message = "댓글 내용은 2000자를 초과할 수 없습니다.")
		String content
) {
}
