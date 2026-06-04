package com.streamingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentResponse {
    private Long id;
    private String title;
    private String description;
    private String contentType;
    private String genre;
    private Double rating;
    private Integer releaseYear;
    private Integer duration;
    private String director;
    private String cast;
    private String posterUrl;
    private LocalDateTime createdAt;
}