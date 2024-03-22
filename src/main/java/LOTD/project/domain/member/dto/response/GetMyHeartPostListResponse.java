package LOTD.project.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMyHeartPostListResponse {

    List<InnerGetMyHeartPost> heartPostList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InnerGetMyHeartPost {

        private Long categoryId;
        private Long postId;
        private String title;
        private Long commentCount;
        private Long hits;
        private LocalDateTime createDateTime;
        private Integer totalPages;
        private Long totalElements;

    }

}
