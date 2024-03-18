package LOTD.project.domain.post.repository;

import LOTD.project.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static LOTD.project.domain.post.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    /**
     * 댓글 수 증가
     * @param commentPost
     */
    @Override
    public void increaseCommentCount(Post commentPost) {
        queryFactory.update(post)
                .set(post.commentsCount, post.commentsCount.add(1))
                .where(post.eq(commentPost))
                .execute();
    }

    /**
     * 댓글 수 감소
     * @param commentDeletePost
     */
    @Override
    public void reduceCommentCount(Post commentDeletePost) {
        queryFactory.update(post)
                .set(post.commentsCount, post.commentsCount.subtract(1))
                .where(post.eq(commentDeletePost))
                .execute();
    }

    /**
     * 좋아요 수 증가
     * @param heartPost
     */
    @Override
    public void increaseHeartCount(Post heartPost) {
        queryFactory.update(post)
                .set(post.heartCount, post.heartCount.add(1))
                .where(post.eq(heartPost))
                .execute();
    }

    /**
     * 좋아요 수 감소
     * @param heartCancelPost
     */
    @Override
    public void reduceHeartCount(Post heartCancelPost) {
        queryFactory.update(post)
                .set(post.heartCount, post.heartCount.subtract(1))
                .where(post.eq(heartCancelPost))
                .execute();
    }

    /**
     * 조회 수 증가
     * @param hitPost
     */
    @Override
    public void increaseHitsCount(Post hitPost) {
        queryFactory.update(post)
                .set(post.hits, post.hits.add(1))
                .where(post.eq(hitPost))
                .execute();
    }
}
