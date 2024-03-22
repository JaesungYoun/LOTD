package LOTD.project.domain.heart.repository;

import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
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

import static LOTD.project.domain.heart.QHeart.heart;
import static LOTD.project.domain.member.QMember.member;
import static LOTD.project.domain.post.QPost.post;

@Repository
@RequiredArgsConstructor
public class HeartRepositoryImpl implements HeartRepositoryCustom{

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<GetMyHeartPostListResponse.InnerGetMyHeartPost> getMyHeartPostList(String memberId, Pageable pageable) {
        List<GetMyHeartPostListResponse.InnerGetMyHeartPost> fetch = queryFactory.select(Projections.bean(GetMyHeartPostListResponse.InnerGetMyHeartPost.class,
                        post.category.categoryId.as("categoryId"),
                        post.postId.as("postId"),
                        post.title.as("title"),
                        post.commentsCount.as("commentsCount"),
                        post.hits.as("hits"),
                        post.member.nickname.as("creator"),
                        post.createDateTime.as("createDateTime")
                ))
                .from(post)
                .innerJoin(post.heart,heart)
                .innerJoin(post.member,member)

                // 동적쿼리를 생성하기 위한 조건문
                .where(isEqualMemberId(memberId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(myHeartListOrder(pageable).stream().toArray(OrderSpecifier[]::new))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .innerJoin(post.heart,heart)
                .innerJoin(post.member,member)
                .where(isEqualMemberId(memberId)
                );

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);


    }


    private BooleanExpression isEqualMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return null;
        }

        return heart.member.memberId.eq(memberId);

    }


    private List<OrderSpecifier> myHeartListOrder(Pageable pageable) {

        List<OrderSpecifier> orders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            PathBuilder pathBuilder = new PathBuilder(post.getType() ,post.getMetadata());

            orders.add(new OrderSpecifier(direction,pathBuilder.get(order.getProperty())));
        }
        return orders;
    }
}
