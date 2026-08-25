package com.planwith.planwith_fo_comment.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_comment.application.command.EventMetadata;
import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.command.HandleReportCommand;
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentLikedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentReportedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.HandleCommentUnlikedUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncStoryProjectionUseCase;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentFullScenarioIntegrationTests {

	private static final String COMMENTS_URL = "/api/planwith-fo-comment/comments";
	private static final String STORY_COMMENTS_URL =
			"/api/planwith-fo-comment/stories/{storyUuid}/comments";
	private static final String MANAGEMENT_URL =
			"/api/planwith-fo-comment/stories/{storyUuid}/comments/management";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SyncStoryProjectionUseCase syncStoryProjectionUseCase;

	@Autowired
	private SyncMemberProjectionUseCase syncMemberProjectionUseCase;

	@Autowired
	private HandleCommentLikedUseCase handleCommentLikedUseCase;

	@Autowired
	private HandleCommentUnlikedUseCase handleCommentUnlikedUseCase;

	@Autowired
	private HandleCommentReportedUseCase handleCommentReportedUseCase;

	@Test
	void verifiesCompleteCommentEventStormingScenario() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID otherMemberUuid = UUID.randomUUID();
		UUID adminUuid = UUID.randomUUID();
		syncStory(storyUuid, storyOwnerUuid);
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				authorUuid,
				"author-v1",
				"https://image.example/author-v1.png",
				"ACTIVE",
				1L
		));

		// 1-2. A member can create a comment, while a guest cannot.
		UUID firstCommentUuid = createComment(storyUuid, authorUuid, null, "first comment");
		mockMvc.perform(post(COMMENTS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createBody(storyUuid, null, "guest comment")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));

		// 3-4. Root comments support latest and like-count ordering.
		Thread.sleep(10);
		UUID latestCommentUuid = createComment(storyUuid, authorUuid, null, "latest comment");
		assertThat(commentUuids(getComments(storyUuid, "LATEST", authorUuid)))
				.containsExactly(latestCommentUuid, firstCommentUuid);

		UUID firstLikeUuid = UUID.randomUUID();
		Instant likeOccurredAt = Instant.now();
		handleCommentLikedUseCase.handleLiked(new HandleLikeCommand(
				firstLikeUuid,
				firstCommentUuid,
				authorUuid,
				event("COMMENT_LIKED", firstLikeUuid, likeOccurredAt)
		));
		assertThat(commentUuids(getComments(storyUuid, "LIKE", authorUuid)))
				.containsExactly(firstCommentUuid, latestCommentUuid);

		// 5-7. One-level replies are returned and only the author can update a comment.
		UUID replyUuid = createComment(storyUuid, otherMemberUuid, firstCommentUuid, "reply");
		JsonNode latestList = getComments(storyUuid, "LATEST", authorUuid);
		assertThat(findComment(latestList, firstCommentUuid).path("replies").get(0).path("commentUuid").asText())
				.isEqualTo(replyUuid.toString());

		mockMvc.perform(patch(COMMENTS_URL + "/{commentUuid}", firstCommentUuid)
						.header("X-Auth-User-Id", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "updated by author"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentContent").value("updated by author"))
				.andExpect(jsonPath("$.canEdit").value(true));
		mockMvc.perform(patch(COMMENTS_URL + "/{commentUuid}", firstCommentUuid)
						.header("X-Auth-User-Id", otherMemberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "unauthorized update"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_OWNER_MISMATCH"));

		// 8-10. Author, Story owner, and ADMIN deletion policies are applied.
		UUID authorDeleteTarget = createComment(storyUuid, authorUuid, null, "author delete target");
		deleteComment(authorDeleteTarget, authorUuid, null);
		assertCommentNotFound(authorDeleteTarget);

		UUID ownerDeleteTarget = createComment(storyUuid, otherMemberUuid, null, "owner delete target");
		deleteComment(ownerDeleteTarget, storyOwnerUuid, null);
		assertCommentNotFound(ownerDeleteTarget);

		UUID adminDeleteTarget = createComment(storyUuid, otherMemberUuid, null, "admin delete target");
		deleteComment(adminDeleteTarget, adminUuid, "ADMIN");
		assertCommentNotFound(adminDeleteTarget);

		// 11-12. Like and unlike events update the projected counter.
		mockMvc.perform(get(COMMENTS_URL + "/{commentUuid}", firstCommentUuid)
						.header("X-Auth-User-Id", authorUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));
		handleCommentUnlikedUseCase.handleUnliked(new HandleLikeCommand(
				firstLikeUuid,
				firstCommentUuid,
				authorUuid,
				event("COMMENT_UNLIKED", firstLikeUuid, likeOccurredAt.plusSeconds(1))
		));
		mockMvc.perform(get(COMMENTS_URL + "/{commentUuid}", firstCommentUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(0));

		// 13-15. Three reports hide the comment and remove it from the public list.
		report(firstCommentUuid, otherMemberUuid, 1);
		mockMvc.perform(get(COMMENTS_URL + "/{commentUuid}", firstCommentUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reportCount").value(1));
		report(firstCommentUuid, otherMemberUuid, 2);
		assertThat(commentUuids(getComments(storyUuid, "LATEST", authorUuid)))
				.doesNotContain(firstCommentUuid);

		// 16-18. Hidden comments are managed by report count and can be deleted by an administrator.
		JsonNode ownerManagement = getManagedComments(storyUuid, storyOwnerUuid, null);
		assertThat(ownerManagement).hasSize(1);
		assertThat(ownerManagement.get(0).path("commentUuid").asText())
				.isEqualTo(firstCommentUuid.toString());
		assertThat(ownerManagement.get(0).path("reportCount").asLong()).isEqualTo(3);

		UUID higherReportCommentUuid = createComment(storyUuid, authorUuid, null, "higher report comment");
		report(higherReportCommentUuid, otherMemberUuid, 4);
		JsonNode sortedManagement = getManagedComments(storyUuid, storyOwnerUuid, null);
		assertThat(commentUuids(sortedManagement))
				.containsExactly(higherReportCommentUuid, firstCommentUuid);
		assertThat(sortedManagement.get(0).path("reportCount").asLong()).isEqualTo(4);
		deleteComment(higherReportCommentUuid, adminUuid, "ADMIN");
		assertThat(commentUuids(getManagedComments(storyUuid, storyOwnerUuid, null)))
				.containsExactly(firstCommentUuid);

		// 19. Kafka redelivery with the same event UUID changes the counter only once.
		UUID duplicateLikeUuid = UUID.randomUUID();
		EventMetadata duplicateMetadata = event("COMMENT_LIKED", duplicateLikeUuid, Instant.now());
		HandleLikeCommand duplicateCommand = new HandleLikeCommand(
				duplicateLikeUuid,
				latestCommentUuid,
				authorUuid,
				duplicateMetadata
		);
		handleCommentLikedUseCase.handleLiked(duplicateCommand);
		handleCommentLikedUseCase.handleLiked(duplicateCommand);
		mockMvc.perform(get(COMMENTS_URL + "/{commentUuid}", latestCommentUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));

		// 20. A newer Member event updates the nickname and profile projection.
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				authorUuid,
				"author-v2",
				"https://image.example/author-v2.png",
				"ACTIVE",
				2L,
				event("MEMBER_UPDATED", authorUuid, Instant.now())
		));
		JsonNode visibleComment = findComment(getComments(storyUuid, "LATEST", authorUuid), latestCommentUuid);
		assertThat(visibleComment.path("member").path("nickname").asText()).isEqualTo("author-v2");
		assertThat(visibleComment.path("member").path("profileImage").asText())
				.isEqualTo("https://image.example/author-v2.png");
	}

	private void syncStory(UUID storyUuid, UUID storyOwnerUuid) {
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				storyOwnerUuid,
				true,
				"ACTIVE",
				1L
		));
	}

	private UUID createComment(UUID storyUuid, UUID memberUuid, UUID parentCommentUuid, String content)
			throws Exception {
		MvcResult result = mockMvc.perform(post(COMMENTS_URL)
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createBody(storyUuid, parentCommentUuid, content)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commentUuid").exists())
				.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
				.path("commentUuid")
				.asText());
	}

	private String createBody(UUID storyUuid, UUID parentCommentUuid, String content) throws Exception {
		return objectMapper.writeValueAsString(new CreateBody(storyUuid, parentCommentUuid, content));
	}

	private JsonNode getComments(UUID storyUuid, String sort, UUID viewerUuid) throws Exception {
		MvcResult result = mockMvc.perform(get(STORY_COMMENTS_URL, storyUuid)
						.param("sort", sort)
						.header("X-Auth-User-Id", viewerUuid))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode getManagedComments(UUID storyUuid, UUID memberUuid, String memberRole) throws Exception {
		var request = get(MANAGEMENT_URL, storyUuid).header("X-Auth-User-Id", memberUuid);
		if (memberRole != null) {
			request.header("X-Auth-Roles", memberRole);
		}
		MvcResult result = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private void deleteComment(UUID commentUuid, UUID memberUuid, String memberRole) throws Exception {
		var request = delete(COMMENTS_URL + "/{commentUuid}", commentUuid)
				.header("X-Auth-User-Id", memberUuid);
		if (memberRole != null) {
			request.header("X-Auth-Roles", memberRole);
		}
		mockMvc.perform(request).andExpect(status().isNoContent());
	}

	private void assertCommentNotFound(UUID commentUuid) throws Exception {
		mockMvc.perform(get(COMMENTS_URL + "/{commentUuid}", commentUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
	}

	private void report(UUID commentUuid, UUID reporterUuid, int count) {
		for (int index = 0; index < count; index++) {
			UUID reportUuid = UUID.randomUUID();
			handleCommentReportedUseCase.handleReported(new HandleReportCommand(
					reportUuid,
					commentUuid,
					reporterUuid,
					event("COMMENT_REPORTED", reportUuid, Instant.now().plusMillis(index))
			));
		}
	}

	private EventMetadata event(String eventType, UUID targetUuid, Instant occurredAt) {
		return new EventMetadata(UUID.randomUUID(), eventType, targetUuid, occurredAt);
	}

	private JsonNode findComment(JsonNode comments, UUID commentUuid) {
		return StreamSupport.stream(comments.spliterator(), false)
				.filter(comment -> commentUuid.toString().equals(comment.path("commentUuid").asText()))
				.findFirst()
				.orElseThrow();
	}

	private List<UUID> commentUuids(JsonNode comments) {
		return StreamSupport.stream(comments.spliterator(), false)
				.map(comment -> UUID.fromString(comment.path("commentUuid").asText()))
				.toList();
	}

	private record CreateBody(UUID storyUuid, UUID parentCommentUuid, String commentContent) {
	}
}
