package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.model.User;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.repository.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class AnnouncementPhotoServiceTest {

    @Inject
    AnnouncementPhotoService service;

    @InjectMock
    AnnouncementRepository announcementRepository;

    @InjectMock
    PhotoRepository photoRepository;

    @InjectMock
    ImageStoreService imageStoreService;

    @InjectMock
    ImageLimitService imageLimitService;

    private User owner;
    private Announcement announcement;

    @BeforeEach
    void setUp() throws Exception {
        owner = new User();
        setEntityId(owner, 100L);

        // Instantiate Announcement via Reflection to bypass protected constructor
        java.lang.reflect.Constructor<Announcement> constructor =
                Announcement.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        announcement = constructor.newInstance();

        setEntityId(announcement, 1L);

        // Assign owner/user
        try {
            Field userField = Announcement.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(announcement, owner);
        } catch (NoSuchFieldException e) {
            Field userField = Announcement.class.getDeclaredField("owner");
            userField.setAccessible(true);
            userField.set(announcement, owner);
        }

        Photo photo1 = new Photo("http://localhost:4566/bucket/images/1.jpg", 0);
        setEntityId(photo1, 10L);
        photo1.setAnnouncement(announcement);

        Photo photo2 = new Photo("http://localhost:4566/bucket/images/2.jpg", 1);
        setEntityId(photo2, 20L);
        photo2.setAnnouncement(announcement);

        Photo photo3 = new Photo("http://localhost:4566/bucket/images/3.jpg", 2);
        setEntityId(photo3, 30L);
        photo3.setAnnouncement(announcement);

        announcement.getPhotos().addAll(List.of(photo1, photo2, photo3));
    }

    @Test
    public void testSortPhotosSuccess() {
        List<PhotoDTO> sortedInput = List.of(createDTO(30L), createDTO(10L), createDTO(20L));

        when(announcementRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(announcement));

        List<PhotoDTO> result = service.sortPhotos(1L, 100L, sortedInput);

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(30L, result.get(0).id);
        assertEquals(0, result.get(0).position);

        assertEquals(10L, result.get(1).id);
        assertEquals(1, result.get(1).position);

        assertEquals(20L, result.get(2).id);
        assertEquals(2, result.get(2).position);
    }

    @Test
    public void testSortPhotosForbidden() {
        Long strangerUserId = 999L;
        List<PhotoDTO> sortedInput = List.of(createDTO(10L), createDTO(20L), createDTO(30L));

        when(announcementRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(announcement));

        DomainException ex = assertThrows(DomainException.class, () ->
                service.sortPhotos(1L, strangerUserId, sortedInput)
        );

        assertEquals(403, ex.getStatus());
        assertEquals("error.unauthorized.access", ex.getMessageKey());
    }

    @Test
    public void testSortPhotosAnnouncementNotFound() {
        when(announcementRepository.findByIdOptional(999L))
                .thenReturn(Optional.empty());

        List<PhotoDTO> sortedInput = List.of(createDTO(10L));

        assertThrows(EntityNotFoundException.class, () ->
                service.sortPhotos(999L, 100L, sortedInput)
        );
    }

    @Test
    public void testSortPhotosSizeMismatch() {
        List<PhotoDTO> sortedInput = List.of(createDTO(10L), createDTO(20L));

        when(announcementRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(announcement));

        FieldValidationException ex = assertThrows(FieldValidationException.class, () ->
                service.sortPhotos(1L, 100L, sortedInput)
        );

        assertEquals("photos", ex.getField());
        assertEquals("photo.mismatch", ex.getMessageKey());
    }

    @Test
    public void testSortPhotosIdMismatch() {
        List<PhotoDTO> sortedInput = List.of(createDTO(10L), createDTO(20L), createDTO(999L));

        when(announcementRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(announcement));

        FieldValidationException ex = assertThrows(FieldValidationException.class, () ->
                service.sortPhotos(1L, 100L, sortedInput)
        );

        assertEquals("photos", ex.getField());
        assertEquals("photo.mismatch", ex.getMessageKey());
    }

    @Test
    public void testSortPhotosEmptyInput() {
        assertThrows(FieldValidationException.class, () -> service.sortPhotos(1L, 100L, null));
        assertThrows(FieldValidationException.class, () -> service.sortPhotos(1L, 100L, Collections.emptyList()));
    }

    private PhotoDTO createDTO(Long id) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = id;
        return dto;
    }

    private void setEntityId(Object entity, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}