package LOTD.project.domain.post.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 조회 목록 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBoardResponse {

    List<InnerGetBoard> getBoardListList = new ArrayList<>();
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerGetBoard {

        private Long categoryId;
        private Long postId;
        private String title;
        private Long commentCount;
        private Long hits;
        private String creator;
        private LocalDateTime createDateTime;
        private Integer totalPages;
        private Long totalElements;

    }


}
