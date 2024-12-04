package backend.dev.review.service;

import backend.dev.facility.entity.FacilityDetails;
import backend.dev.review.dto.ReviewDTO;
import backend.dev.review.entity.Review;
import backend.dev.review.repository.ReviewRepository;
import backend.dev.tag.entity.Tag;
import backend.dev.tag.enums.TagValue;
import backend.dev.tag.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private backend.dev.facility.repository.FacilityDetailsRepository facilityDetailsRepository;

    private FacilityDetails testFacility;
    private Review testReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testFacility = new FacilityDetails();
        testFacility.changeFacilityDetailsId("test-facility-id");
        testFacility.changeFacilityName("잠실야구장");

        testReview = new Review();
        testReview.setReviewId(1L);
        testReview.setFacility(testFacility);
        testReview.setReview_content("Test Review");
        testReview.setReview_rating(4.5);
        testReview.setReviewLikes(10);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateReview() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setContent("훌륭한 시설입니다!");
        reviewDTO.setRating(5.0);
        reviewDTO.setTags(List.of(TagValue.CLEAN, TagValue.GOOD_LOCATION));

        when(facilityDetailsRepository.findById("test-facility-id")).thenReturn(Optional.of(testFacility));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setReviewId(2L);
            savedReview.setCreatedAt(LocalDateTime.now());
            savedReview.setUpdatedAt(LocalDateTime.now());
            return savedReview;
        });

        when(tagRepository.findByReviewReviewId(2L)).thenReturn(
                List.of(
                        new Tag(testReview, TagValue.CLEAN),
                        new Tag(testReview, TagValue.GOOD_LOCATION)
                )
        );

        ReviewDTO createdReview = reviewService.createReview("test-facility-id", reviewDTO);

        assertThat(createdReview.getReviewId()).isEqualTo(2L);
        assertThat(createdReview.getContent()).isEqualTo("훌륭한 시설입니다!");
        assertThat(createdReview.getRating()).isEqualTo(5.0);
        assertThat(createdReview.getTags()).containsExactly(TagValue.CLEAN, TagValue.GOOD_LOCATION);
        assertThat(createdReview.getCreatedAt()).isNotNull();
        assertThat(createdReview.getUpdatedAt()).isNotNull();

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testGetReviewsByFacility() {
        when(reviewRepository.findByFacility_FacilityIdOrderByCreatedAtDesc("test-facility-id"))
                .thenReturn(Arrays.asList(testReview));

        when(tagRepository.findByReviewReviewId(1L)).thenReturn(
                List.of(
                        new Tag(testReview, TagValue.CLEAN),
                        new Tag(testReview, TagValue.GOOD_LOCATION)
                )
        );

        List<ReviewDTO> reviews = reviewService.getReviewsByFacility("test-facility-id");

        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getContent()).isEqualTo("Test Review");
        assertThat(reviews.get(0).getRating()).isEqualTo(4.5);
        assertThat(reviews.get(0).getTags()).containsExactly(TagValue.CLEAN, TagValue.GOOD_LOCATION);
        assertThat(reviews.get(0).getContent()).isNotNull();
        assertThat(reviews.get(0).getUpdatedAt()).isNotNull();

        verify(reviewRepository, times(1)).findByFacility_FacilityIdOrderByCreatedAtDesc("test-facility-id");
    }

    @Test
    void testUpdateReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review updatedReview = invocation.getArgument(0);
            updatedReview.setUpdatedAt(LocalDateTime.now());
            return updatedReview;
        });

        ReviewDTO updatedDTO = new ReviewDTO();
        updatedDTO.setContent("Updated Test Review");
        updatedDTO.setRating(5.0);

        ReviewDTO updatedReview = reviewService.updateReview(1L, updatedDTO);

        assertThat(updatedReview.getContent()).isEqualTo("Updated Test Review");
        assertThat(updatedReview.getRating()).isEqualTo(5.0);
        assertThat(updatedReview.getCreatedAt()).isNotNull();
        assertThat(updatedReview.getUpdatedAt()).isNotNull();

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testDeleteReview() {
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).deleteById(1L);
    }
}