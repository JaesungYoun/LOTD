package LOTD.project.domain.post.repository;


import LOTD.project.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAll(Pageable pageable);

    Page<Post> findAllByCreatorContains(String creator, Pageable pageable); // 작성자로 검색
    Page<Post> findAllByTitleContains(String text, Pageable pageable); // 제목 + 내용으로 검색
    Page <Post> findAllByContentContains(String text, Pageable pageable); // 제목 + 내용으로 검색

    Optional<Post> findByPostId(Long postId);

}
