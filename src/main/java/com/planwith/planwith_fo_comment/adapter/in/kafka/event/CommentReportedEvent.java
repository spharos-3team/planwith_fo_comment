package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentReportedEvent(
		UUID reportUuid,
		UUID commentUuid,
		UUID memberUuid
) {
}
