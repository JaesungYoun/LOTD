package LOTD.project.domain.post.repository;

import LOTD.project.domain.member.dto.response.GetMyCommentPostListResponse;
import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
import LOTD.project.domain.post.Post;
import LOTD.project.domain.post.dto.response.GetBoardResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static LOTD.project.domain.comment.QComment.comment;
import static LOTD.project.domain.heart.QHeart.heart;
import static LOTD.project.domain.member.QMember.member;
import static LOTD.project.domain.post.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;


    /**
     * 게시글 목록 조회
     * @param searchCondition
     * @param text
     * @param pageable
     * @return
     */
    @Override
    public Page<GetBoardResponse.InnerGetBoard> getBoardList(Long categoryId, String searchCondition, String text, Pageable pageable) {

        List<GetBoardResponse.InnerGetBoard> fetch = queryFactory.select(Projections.bean(GetBoardResponse.InnerGetBoard.class,
                    post.category.categoryId.as("categoryId"),
                    post.postId.as("postId"),
                    post.title.as("title"),
                    post.commentCount.as("commentCount"),
                    post.hits.as("hits"),
                    post.member.nickname.as("creator"),
                    post.createDateTime.as("createDateTime")
                ))
                .from(post)

                // 동적쿼리를 생성하기 위한 조건문
                .where(searchCondition(searchCondition,text)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(boardOrder(pageable).stream().toArray(OrderSpecifier[]::new))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(isEqualCategoryId(categoryId),
                        searchCondition(searchCondition,text)
                );

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<GetMyHeartPostListResponse.InnerGetMyHeartPost> getMyHeartPostList(String memberId, Pageable pageable) {
        List<GetMyHeartPostListResponse.InnerGetMyHeartPost> fetch = queryFactory.select(Projections.bean(GetMyHeartPostListResponse.InnerGetMyHeartPost.class,
                        post.category.categoryId.as("categoryId"),
                        post.postId.as("postId"),
                        post.title.as("title"),
                        post.commentCount.as("commentCount"),
                        post.hits.as("hits"),
                        post.createDateTime.as("createDateTime")
                ))
                .from(post)
                .leftJoin(post.heart,heart)
                .innerJoin(post.member, member)
                .where(heart.isNotNull(),
                        isEqualHeartMemberId(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(boardOrder(pageable).stream().toArray(OrderSpecifier[]::new))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.heart,heart)
                .innerJoin(post.member, member)
                .where(heart.isNotNull(),
                        isEqualHeartMemberId(memberId)
                );

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);


    }

    @Override
    public Page<GetMyCommentPostListResponse.InnerGetMyCommentPost> getMyCommentPostList(String memberId, Pageable pageable) {
        List<GetMyCommentPostListResponse.InnerGetMyCommentPost> fetch = queryFactory
                .select(Projections.bean(GetMyCommentPostListResponse.InnerGetMyCommentPost.class,
                        post.category.categoryId.as("categoryId"),
                        post.postId.as("postId"),
                        post.title.as("postTitle"),
                        comment.content.as("commentContent"),
                        post.commentCount.as("commentCount"),
                        post.hits.as("postHits"),
                        post.createDateTime.as("createDateTime")

                ))
                .from(post)
                .leftJoin(post.comment,comment)
                .innerJoin(post.member, member)
                .where(comment.isNotNull(),
                        isEqualCommentMemberId(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(boardOrder(pageable).stream().toArray(OrderSpecifier[]::new))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.comment,comment)
                .innerJoin(post.member, member)
                .where(comment.isNotNull(),
                        isEqualCommentMemberId(memberId)
                );

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);

    }

    /**
     * 댓글 수 증가
     * @param commentPost
     */
    @Override
    public void increaseCommentCount(Post commentPost) {
        queryFactory.update(post)
                .set(post.commentCount, post.commentCount.add(1))
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
                .set(post.commentCount, post.commentCount.subtract(1))
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


    private BooleanExpression isEqualCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return post.category.categoryId.eq(categoryId);
    }

    private BooleanExpression searchCondition(String searchCondition, String text) {
        if (searchCondition == null || searchCondition.isBlank()) {
            return null;
        }

        if (text == null || text.isBlank()) {
            return null;
        }

        if (searchCondition.equals("creator")) {
            return post.member.nickname.eq(text);
        }
        else if (searchCondition.equals("title")) {
            return post.title.contains(text);
        }
        else if (searchCondition.equals("content")) {
            return post.content.contains(text);
        }
        else
            return null;
    }

    private BooleanExpression isEqualHeartMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return null;
        }

        return heart.member.memberId.eq(memberId);

    }
    private BooleanExpression isEqualCommentMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return null;
        }

        return comment.member.memberId.eq(memberId);

    }


    private List<OrderSpecifier> boardOrder(Pageable pageable) {

        List<OrderSpecifier> orders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            PathBuilder pathBuilder = new PathBuilder(post.getType() ,post.getMetadata());

            orders.add(new OrderSpecifier(direction,pathBuilder.get(order.getProperty())));
        }
        return orders;
    }



}
