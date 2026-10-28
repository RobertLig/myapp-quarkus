package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.model.User;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    AnnouncementRepository announcementRepository;

    @InjectMock
    UserBlockRepository userBlockRepository;

    @InjectMock
    ImageStoreService imageStoreService;

    @Test
    void deleteUserRemovesAvatarAndAnnouncementPhotos() {
        Long userId = 42L;
        String avatarUrl = "http://localhost:4566/my-local-test-bucket/images/avatar";
        String announcementPhotoUrl = "http://localhost:4566/my-local-test-bucket/images/announcement";
        User user = new User();
        user.setPhotoUrl(avatarUrl);
        Announcement announcement = new Announcement("sender", user);
        announcement.getPhotos().add(new Photo(announcementPhotoUrl, 0));

        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));
        when(announcementRepository.list("user.id", userId)).thenReturn(List.of(announcement));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
        verify(imageStoreService).deleteAll(List.of(announcementPhotoUrl, avatarUrl));
    }
}
