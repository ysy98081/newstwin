package com.est.newstwin.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubscriptionRequestDto {
    private Boolean receiveEmail;
    private List<Long> categoryIds; // 선택된 카테고리 id 리스트
}
