package LOTD.project.domain.member.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GetMyCommentPostListResponse {

    List<InnerGetMyCommentPost> commentsPostList;

    @Data
    @Builder
    public static class InnerGetMyCommentPost {

        private Long categoryId;
        private Long postId;
        private String postTitle;
        private String commentContent;
        private Long commentCount;
        private Long postHits;
        private LocalDateTime createDateTime;
        private Integer totalPages;
        private Long totalElements;

    }

}
