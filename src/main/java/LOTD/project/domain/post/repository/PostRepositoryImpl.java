package LOTD.project.domain.post.repository;

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
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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
                    post.commentsCount.as("commentsCount"),
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
