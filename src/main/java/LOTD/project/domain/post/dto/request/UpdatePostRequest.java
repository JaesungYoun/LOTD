package LOTD.project.domain.post.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class UpdatePostRequest {

    @NotNull
    private Long categoryId;
    @NotBlank
    private String title;

    private String content;
    private String image;


}
