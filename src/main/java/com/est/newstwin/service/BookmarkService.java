package com.est.newstwin.service;

import com.est.newstwin.domain.Bookmark;
import com.est.newstwin.repository.BookmarkRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public boolean toggle(Long postId, Long memberId) {
    if (bookmarkRepository.existsByPostIdAndMemberId(postId, memberId)) {
      bookmarkRepository.deleteByPostIdAndMemberId(postId, memberId);
      return false;
    } else {
      try{
        Bookmark bookmark = Bookmark.builder()
            .member(memberRepository.getReferenceById(memberId))
            .post(postRepository.getReferenceById(postId))
            .build();
        bookmarkRepository.save(bookmark);
        return true;
      }catch(Exception e){
        bookmarkRepository.deleteByPostIdAndMemberId(postId, memberId);
        return false;
      }
    }
  }

  public boolean isBookmarked(Long postId, Long memberId) {
    return bookmarkRepository.existsByPostIdAndMemberId(postId, memberId);
  }
}