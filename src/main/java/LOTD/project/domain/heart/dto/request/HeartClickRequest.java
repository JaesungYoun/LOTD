package LOTD.project.domain.heart.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartClickRequest {

    private String memberId;
    private Long postId;
}
