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

		@Schema(description = "부모 댓글 UUID. 없으면 일반 댓글, 있으면 1단계 대댓글", example = "22222222-2222-2222-2222-222222222222")
		UUID parentCommentUuid,

		@Schema(description = "댓글 내용", example = "좋은 일정이에요.")
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(min = 1, max = 1000, message = "댓글 내용은 1자 이상 1000자 이하여야 합니다.")
		String content
) {
}
