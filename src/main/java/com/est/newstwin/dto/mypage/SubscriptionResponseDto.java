package com.est.newstwin.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponseDto {
    private Boolean receiveEmail;
    private List<String> subscribedCategories;
}
