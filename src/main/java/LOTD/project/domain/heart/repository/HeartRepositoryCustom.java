package LOTD.project.domain.heart.repository;


import LOTD.project.domain.member.dto.response.GetMyHeartPostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HeartRepositoryCustom {

    Page<GetMyHeartPostListResponse.InnerGetMyHeartPost> getMyHeartPostList(String memberId,Pageable pageable);


}
