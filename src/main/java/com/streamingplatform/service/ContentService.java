package com.streamingplatform.service;

import com.streamingplatform.dto.ContentResponse;
import com.streamingplatform.entity.Content;
import com.streamingplatform.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    public Page<ContentResponse> getAllContent(Pageable pageable) {
        log.info("Fetching all content");
        Page<Content> content = contentRepository.findAll(pageable);
        return mapToResponsePage(content);
    }

    public ContentResponse getContentById(Long id) {
        log.info("Fetching content with ID: {}", id);
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        return mapToResponse(content);
    }

    public Page<ContentResponse> searchByTitle(String title, Pageable pageable) {
        log.info("Searching content by title: {}", title);
        Page<Content> content = contentRepository.findByTitleContainingIgnoreCase(title, pageable);
        return mapToResponsePage(content);
    }

    public Page<ContentResponse> getContentByGenre(String genre, Pageable pageable) {
        log.info("Fetching content by genre: {}", genre);
        Page<Content> content = contentRepository.findByGenre(genre, pageable);
        return mapToResponsePage(content);
    }

    public Page<ContentResponse> getTrendingContent(Pageable pageable) {
        log.info("Fetching trending content");
        List<Content> trendingContent = contentRepository.findByIsActiveTrueOrderByRatingDesc();
        List<ContentResponse> responseList = trendingContent.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responseList, pageable, responseList.size());
    }

    @Transactional
    public ContentResponse createContent(Content content) {
        log.info("Creating new content: {}", content.getTitle());
        Content savedContent = contentRepository.save(content);
        return mapToResponse(savedContent);
    }

    private ContentResponse mapToResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .contentType(content.getContentType().toString())
                .genre(content.getGenre())
                .rating(content.getRating())
                .releaseYear(content.getReleaseYear())
                .duration(content.getDuration())
                .director(content.getDirector())
                .cast(content.getCast())
                .posterUrl(content.getPosterUrl())
                .createdAt(content.getCreatedAt())
                .build();
    }

    private Page<ContentResponse> mapToResponsePage(Page<Content> contentPage) {
        return contentPage.map(this::mapToResponse);
    }
}