package LOTD.project.domain.comment.dto.response;

import LOTD.project.domain.comment.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class GetCommentListResponse {

    List<InnerComment> commentList = new ArrayList<>();


    @Data
    @Builder
    public static class InnerComment {

        private String memberId;
        private String creator;
        private Long commentId;
        private Long parentCommentId;
        private String content;
        private String deleteStatus;
        private LocalDateTime createdDate;
    }


    /**
     public GetCommentListResponse(Comment comment) {
     this.commentId = comment.getCommentId();
     this.parentCommentId = comment.getParentCommentId();
     this.content = comment.getContent();
     }*/

}
