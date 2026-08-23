package com.planwith.planwith_fo_comment.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.SyncMemberProjectionUseCase;
import com.planwith.planwith_fo_comment.application.port.in.SyncStoryProjectionUseCase;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentWebAdapterIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SyncStoryProjectionUseCase syncStoryProjectionUseCase;

	@Autowired
	private SyncMemberProjectionUseCase syncMemberProjectionUseCase;

	@Test
	void createListDetailUpdateAndDeleteThroughWebAdapter() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		enableStory(storyUuid);

		MvcResult created = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "웹 어댑터 댓글"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commentUuid").exists())
				.andExpect(jsonPath("$.commentContent").value("웹 어댑터 댓글"))
				.andReturn();

		JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
		String commentUuid = body.get("commentUuid").asText();

		mockMvc.perform(get("/api/planwith-fo-comment/comments")
						.param("storyUuid", storyUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].commentUuid").value(commentUuid));

		mockMvc.perform(get("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(storyUuid.toString()))
				.andExpect(jsonPath("$.parentCommentUuid").isEmpty());

		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "content": "수정된 웹 댓글"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentContent").value("수정된 웹 댓글"));

		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", memberUuid))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
	}

	@Test
	void createReplyThroughWebAdapter() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		enableStory(storyUuid);

		MvcResult parent = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "부모 댓글"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();

		String parentUuid = objectMapper.readTree(parent.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "parentCommentUuid": "%s",
								  "commentContent": "대댓글"
								}
								""".formatted(storyUuid, parentUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.parentCommentUuid").value(parentUuid))
				.andExpect(jsonPath("$.commentContent").value("대댓글"))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void createCommentReturnsCreatedBodyImmediately() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		enableStory(storyUuid);
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				memberUuid,
				"여행자",
				"https://image.example/profile.png",
				"ACTIVE"
		));

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "parentCommentUuid": null,
								  "commentContent": "좋은 여행이네요!"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commentUuid").exists())
				.andExpect(jsonPath("$.storyUuid").value(storyUuid.toString()))
				.andExpect(jsonPath("$.memberUuid").value(memberUuid.toString()))
				.andExpect(jsonPath("$.parentCommentUuid").isEmpty())
				.andExpect(jsonPath("$.commentContent").value("좋은 여행이네요!"))
				.andExpect(jsonPath("$.nickname").value("여행자"))
				.andExpect(jsonPath("$.profileImage").value("https://image.example/profile.png"))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.reportCount").value(0))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void createCommentRejectsLegacyContentField() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		enableStory(storyUuid);

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "content": "레거시 필드"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void createCommentFailsWithoutMemberHeader() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "11111111-1111-1111-1111-111111111111",
								  "commentContent": "헤더 없음"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"))
				.andExpect(jsonPath("$.message").value("로그인 후 댓글을 작성할 수 있습니다."));
	}

	@Test
	void guestCanQueryCommentsWithoutLogin() throws Exception {
		UUID storyUuid = UUID.randomUUID();

		mockMvc.perform(get("/api/planwith-fo-comment/comments")
						.param("storyUuid", storyUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void createCommentFailsWhenStoryIsMissingOrDisabled() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID missingStoryUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "없는 스토리"
								}
								""".formatted(missingStoryUuid)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("STORY_NOT_FOUND"));

		UUID disabledStoryUuid = UUID.randomUUID();
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				disabledStoryUuid,
				UUID.randomUUID(),
				false,
				"ACTIVE",
				1L
		));
		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "댓글 비허용"
								}
								""".formatted(disabledStoryUuid)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_ALLOWED"));
	}

	@Test
	void createCommentFailsWhenContentIsBlank() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		enableStory(storyUuid);

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": " "
								}
								""".formatted(storyUuid)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	private void enableStory(UUID storyUuid) {
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				UUID.randomUUID(),
				true,
				"ACTIVE",
				1L
		));
	}
}
