package LOTD.project.domain.member.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GetMyCommentsPostListResponse {

    List<InnerGetMyCommentsPost> commentsPostList;

    @Data
    @Builder
    public static class InnerGetMyCommentsPost {

        private Long categoryId;
        private Long postId;
        private String title;
        private Long commentsCount;
        private Long hits;
        private String creator;
        private LocalDateTime createDateTime;
        private Integer totalPages;
        private Long totalElements;

    }

}
