package LOTD.project.domain.post;
import LOTD.project.domain.category.Category;
import LOTD.project.domain.comment.Comment;
import LOTD.project.domain.heart.Heart;
import LOTD.project.domain.member.Member;
import LOTD.project.global.audit.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "post")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Setter
@DynamicInsert
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long postId; // 게시글 아이디


    @Column(nullable = false, name = "TITLE")
    @NotBlank
    private String title; // 제목

    @Column(name = "CONTENT")
    private String content; // 내용

    @Column(name = "IMAGE")
    private String image; // 대표 이미지

    @Column(nullable = false, name = "COMMENT_COUNT")
    @Builder.Default
    private Long commentCount = 0L; // 댓글 수

    @Column(nullable = false, name = "HEART_COUNT")
    @Builder.Default
    private Long heartCount = 0L; // 좋아요 수

    @Column(nullable = false, name = "HIT_COUNT")
    @Builder.Default
    private Long hits = 0L; // 조회 수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category; // 카테고리 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Comment> comment;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Heart> heart;

    public void updatePost(Category category, String title, String content, String image) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.image = image;
    }

}
