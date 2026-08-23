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
import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;
import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;
import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;
import com.planwith.planwith_fo_comment.application.port.in.HandleLikeCreatedUseCase;
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

	@Autowired
	private HandleLikeCreatedUseCase handleLikeCreatedUseCase;

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

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].commentUuid").value(commentUuid))
				.andExpect(jsonPath("$[0].commentContent").value("웹 어댑터 댓글"))
				.andExpect(jsonPath("$[0].commentLikeCount").value(0))
				.andExpect(jsonPath("$[0].replies").isArray());

		mockMvc.perform(get("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(storyUuid.toString()))
				.andExpect(jsonPath("$.parentCommentUuid").isEmpty());

		Thread.sleep(10);
		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "수정된 웹 댓글"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentContent").value("수정된 웹 댓글"))
				.andExpect(jsonPath("$.isUpdated").value(true));

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

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void listCommentsByStorySupportsSortRepliesAndViewerFlags() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		enableStory(storyUuid);
		syncMemberProjectionUseCase.sync(new SyncMemberProjectionCommand(
				authorUuid,
				"작성자",
				"https://image.example/author.png",
				"ACTIVE"
		));

		MvcResult older = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "이전 댓글"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		String olderUuid = objectMapper.readTree(older.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();
		Thread.sleep(10);

		MvcResult newer = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", otherUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "최신 댓글"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		String newerUuid = objectMapper.readTree(newer.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();

		mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "parentCommentUuid": "%s",
								  "commentContent": "대댓글"
								}
								""".formatted(storyUuid, olderUuid)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid)
						.param("sort", "LATEST")
						.header("X-Member-Uuid", authorUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].commentUuid").value(newerUuid))
				.andExpect(jsonPath("$[1].commentUuid").value(olderUuid))
				.andExpect(jsonPath("$[1].member.memberUuid").value(authorUuid.toString()))
				.andExpect(jsonPath("$[1].member.nickname").value("작성자"))
				.andExpect(jsonPath("$[1].member.profileImage").value("https://image.example/author.png"))
				.andExpect(jsonPath("$[1].replies[0].commentContent").value("대댓글"))
				.andExpect(jsonPath("$[1].replies[0].parentCommentUuid").value(olderUuid))
				.andExpect(jsonPath("$[1].canEdit").value(true))
				.andExpect(jsonPath("$[1].canDelete").value(true))
				.andExpect(jsonPath("$[0].canEdit").value(false))
				.andExpect(jsonPath("$[1].isUpdated").value(false));

		Thread.sleep(10);
		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", olderUuid)
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "수정된 이전 댓글"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid)
						.param("sort", "LATEST")
						.header("X-Member-Uuid", authorUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[1].isUpdated").value(true))
				.andExpect(jsonPath("$[1].commentContent").value("수정된 이전 댓글"));

		handleLikeCreatedUseCase.handleCreated(new HandleLikeCommand(
				UUID.randomUUID(),
				UUID.fromString(olderUuid),
				authorUuid
		));
		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid)
						.param("sort", "LIKE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].commentUuid").value(olderUuid))
				.andExpect(jsonPath("$[0].commentLikeCount").value(1))
				.andExpect(jsonPath("$[1].commentUuid").value(newerUuid));

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid)
						.param("sort", "UNKNOWN"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
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
	void updateCommentAllowsAuthorOnlyAndRejectsOthers() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				storyOwnerUuid,
				true,
				"ACTIVE",
				1L
		));

		MvcResult created = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "원본 댓글"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		String commentUuid = objectMapper.readTree(created.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();

		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "비회원 수정"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));

		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "다른 사용자 수정"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_OWNER_MISMATCH"));

		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", storyOwnerUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "스토리 주인 수정"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_OWNER_MISMATCH"));

		Thread.sleep(10);
		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentContent": "작성자 수정"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentContent").value("작성자 수정"))
				.andExpect(jsonPath("$.updatedAt").exists())
				.andExpect(jsonPath("$.isUpdated").value(true));

		mockMvc.perform(patch("/api/planwith-fo-comment/comments/{commentUuid}", commentUuid)
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "content": "레거시 필드"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void deleteCommentAllowsAuthorStoryOwnerAndAdminAndKeepsReplies() throws Exception {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID storyOwnerUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		syncStoryProjectionUseCase.sync(new SyncStoryProjectionCommand(
				storyUuid,
				storyOwnerUuid,
				true,
				"ACTIVE",
				1L
		));

		MvcResult parent = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
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
						.header("X-Member-Uuid", otherUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "parentCommentUuid": "%s",
								  "commentContent": "대댓글"
								}
								""".formatted(storyUuid, parentUuid)))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", parentUuid))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));
		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", parentUuid)
						.header("X-Member-Uuid", otherUuid))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_DELETE_FORBIDDEN"));

		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", parentUuid)
						.header("X-Member-Uuid", authorUuid))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/planwith-fo-comment/stories/{storyUuid}/comments", storyUuid)
						.header("X-Member-Uuid", storyOwnerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].commentUuid").value(parentUuid))
				.andExpect(jsonPath("$[0].commentContent").value("삭제된 댓글입니다."))
				.andExpect(jsonPath("$[0].isDeleted").value(true))
				.andExpect(jsonPath("$[0].canEdit").value(false))
				.andExpect(jsonPath("$[0].canDelete").value(false))
				.andExpect(jsonPath("$[0].replies[0].commentContent").value("대댓글"))
				.andExpect(jsonPath("$[0].replies[0].canDelete").value(true));

		MvcResult ownerTarget = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "스토리 주인이 삭제"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		String ownerTargetUuid = objectMapper.readTree(ownerTarget.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();
		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", ownerTargetUuid)
						.header("X-Member-Uuid", storyOwnerUuid))
				.andExpect(status().isNoContent());

		MvcResult adminTarget = mockMvc.perform(post("/api/planwith-fo-comment/comments")
						.header("X-Member-Uuid", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "storyUuid": "%s",
								  "commentContent": "운영자가 삭제"
								}
								""".formatted(storyUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		String adminTargetUuid = objectMapper.readTree(adminTarget.getResponse().getContentAsString())
				.get("commentUuid")
				.asText();
		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", adminTargetUuid)
						.header("X-Member-Uuid", UUID.randomUUID())
						.header("X-Member-Role", "ADMIN"))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/api/planwith-fo-comment/comments/{commentUuid}", parentUuid)
						.header("X-Member-Uuid", authorUuid))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMMENT_ALREADY_DELETED"));
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
