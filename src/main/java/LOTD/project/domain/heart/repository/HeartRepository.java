package LOTD.project.domain.heart.repository;

import LOTD.project.domain.heart.Heart;
import LOTD.project.domain.member.Member;
import LOTD.project.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {

    Optional<Heart> findByMemberAndPost(Member member, Post post);


}
