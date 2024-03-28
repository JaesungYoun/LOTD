package LOTD.project.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMyCommentPostListResponse {

    List<InnerGetMyCommentPost> commentsPostList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerGetMyCommentPost {

        private Long categoryId;
        private Long postId;
        private String title;
        private String commentContent;
        private Long commentCount;
        private Long postHits;
        private LocalDateTime createDateTime;
        private Integer totalPages;
        private Long totalElements;

    }

}
