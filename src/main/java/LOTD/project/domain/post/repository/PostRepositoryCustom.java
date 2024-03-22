package LOTD.project.domain.post.repository;

import LOTD.project.domain.post.Post;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {


    Page<GetBoardResponse.InnerGetBoard> getBoardList(Long categoryId, String searchCondition, String text, Pageable pageable);


    void increaseCommentCount(Post commentPost);
    void reduceCommentCount(Post commentDeletePost);
    void increaseHeartCount(Post heartPost);
    void reduceHeartCount(Post heartCancelPost);
    void increaseHitsCount(Post hitPost);

}
