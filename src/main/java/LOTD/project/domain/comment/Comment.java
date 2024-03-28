package LOTD.project.domain.comment;


import LOTD.project.domain.member.Member;
import LOTD.project.domain.post.Post;
import LOTD.project.global.audit.BaseEntity;
import LOTD.project.global.status.DeleteStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_COMMENT_ID")
    private Comment parentComment;

    @Column(name = "CONTENT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToMany(mappedBy = "parentComment", orphanRemoval = true)
    private List<Comment> children;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "DELETE_STATUS")
    @Builder.Default
    private DeleteStatus isDeleted = DeleteStatus.N;


    public void updateComment(String content) {
        this.content = content;
    }

    public void updateDeleteStatus(DeleteStatus deleteStatus) {
        this.isDeleted = deleteStatus;
    }


}
