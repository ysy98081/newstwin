package com.est.newstwin.service;

import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final UserSubscriptionRepository subscriptionRepository;
  private final MailLogRepository mailRepository;

  public long getUserCount() {
    return memberRepository.count();
  }

  public long getPostCount() {
    return postRepository.count();
  }

  public long getSubscriptionCount() {
    return subscriptionRepository.count();
  }

  public long getMailCount() {
    return mailRepository.count();
  }
}