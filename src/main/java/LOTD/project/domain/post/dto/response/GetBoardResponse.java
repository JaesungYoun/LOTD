package LOTD.project.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 조회 목록 응답
 */
@Getter
@Builder
public class GetBoardResponse {

    List<InnerGetBoard> getBoardListList = new ArrayList<>();
    @Getter
    @Builder
    public static class InnerGetBoard {

        private Long categoryId;
        private Long postId;
        private String title;
        private Long commentsCount;
        private Long hits;
        private String creator;
        private LocalDateTime createdDateTime;
        private Integer totalPages;
        private Long totalElements;

    }


}
