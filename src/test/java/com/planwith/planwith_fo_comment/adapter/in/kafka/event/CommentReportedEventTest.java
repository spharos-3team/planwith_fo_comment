package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class CommentReportedEventTest {

	@Test
	void ignoresReportReasonFieldsOwnedByReportService() throws Exception {
		UUID reportUuid = UUID.randomUUID();
		UUID commentUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		ObjectMapper objectMapper = new ObjectMapper();

		CommentReportedEvent event = objectMapper.readValue("""
				{
				  "reportUuid": "%s",
				  "commentUuid": "%s",
				  "memberUuid": "%s",
				  "reportType": "OTHER",
				  "reasonDetail": "기타 사유"
				}
				""".formatted(reportUuid, commentUuid, memberUuid), CommentReportedEvent.class);

		assertThat(event.reportUuid()).isEqualTo(reportUuid);
		assertThat(event.commentUuid()).isEqualTo(commentUuid);
		assertThat(event.memberUuid()).isEqualTo(memberUuid);
	}
}
