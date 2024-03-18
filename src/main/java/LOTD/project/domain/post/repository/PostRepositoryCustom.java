package LOTD.project.domain.post.repository;

import LOTD.project.domain.post.Post;

public interface PostRepositoryCustom {

    void increaseCommentCount(Post commentPost);
    void reduceCommentCount(Post commentDeletePost);
    void increaseHeartCount(Post heartPost);
    void reduceHeartCount(Post heartCancelPost);
    void increaseHitsCount(Post hitPost);

}
