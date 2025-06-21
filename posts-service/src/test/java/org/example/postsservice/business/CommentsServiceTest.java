package org.example.postsservice.business;

import org.example.postsservice.dto.CommentResponseDTO;
import org.example.postsservice.models.comments.Comment;
import org.example.postsservice.repositories.CommentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CommentsServiceTest {

    @Mock
    private CommentsRepository commentsRepository;

    @InjectMocks
    private CommentsService commentsService;

    @Test
    void getCommentsForPost_shouldReturnPagedComments() {
        Long postId = 1L;
        int page = 0;

        List<CommentResponseDTO> mockComments = List.of(
                new MockCommentResponseDTO("alice", new Date(), "Nice!", "profile1.jpg"),
                new MockCommentResponseDTO("bob", new Date(), "Great spot!", "profile2.jpg")
        );

        Page<CommentResponseDTO> mockPage = new PageImpl<>(mockComments);
        when(commentsRepository.findByPostId(eq(postId), any(Pageable.class))).thenReturn(mockPage);

        Page<CommentResponseDTO> result = commentsService.getCommentsForPost(postId, page);

        assertEquals(2, result.getTotalElements());
        assertEquals("alice", result.getContent().get(0).getCreatedBy());
    }


    @Test
    void addComment_shouldSaveCommentToRepository() {
        String createdBy = "user123";
        Long postId = 5L;
        String content = "Amazing car!";

        commentsService.addComment(createdBy, postId, content);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentsRepository).save(captor.capture());

        Comment savedComment = captor.getValue();
        assertEquals(createdBy, savedComment.getCreatedBy());
        assertEquals(postId, savedComment.getPostId());
        assertEquals(content, savedComment.getContent());
    }
}
