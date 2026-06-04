package com.streamingplatform.service;

import com.streamingplatform.entity.WatchHistory;
import com.streamingplatform.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;

    public Page<WatchHistory> getUserWatchHistory(Long userId, Pageable pageable) {
        log.info("Fetching watch history for user: {}", userId);
        return watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable);
    }

    public List<WatchHistory> getRecentlyWatched(Long userId) {
        log.info("Fetching recently watched for user: {}", userId);
        return watchHistoryRepository.findTop10ByUserIdOrderByWatchedAtDesc(userId);
    }

    public WatchHistory recordWatchHistory(Long userId, Long contentId, Integer watchedDuration, Boolean isCompleted) {
        log.info("Recording watch history - User: {}, Content: {}", userId, contentId);
        List<WatchHistory> existingHistory = watchHistoryRepository.findByUserIdAndContentId(userId, contentId);

        WatchHistory watchHistory;
        if (!existingHistory.isEmpty()) {
            watchHistory = existingHistory.get(0);
            watchHistory.setWatchedDuration(watchedDuration);
            watchHistory.setIsCompleted(isCompleted);
        } else {
            watchHistory = WatchHistory.builder()
                    .userId(userId)
                    .contentId(contentId)
                    .watchedDuration(watchedDuration)
                    .isCompleted(isCompleted)
                    .build();
        }
        return watchHistoryRepository.save(watchHistory);
    }
}