package com.planwith.planwith_fo_comment.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

class CommentOutboxJpaEntityMappingTest {

	@Test
	@DisplayName("Outbox payload는 255자를 초과하는 이벤트 JSON을 위해 TEXT 컬럼을 사용한다")
	void mapsPayloadToTextColumn() throws NoSuchFieldException {
		Field payloadField = CommentOutboxJpaEntity.class.getDeclaredField("payload");
		Column column = payloadField.getAnnotation(Column.class);

		assertThat(column).isNotNull();
		assertThat(column.columnDefinition()).isEqualTo("text");
		assertThat(payloadField.isAnnotationPresent(Lob.class)).isFalse();
	}
}
