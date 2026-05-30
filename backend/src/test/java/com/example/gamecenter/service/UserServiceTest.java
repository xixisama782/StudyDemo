package com.example.gamecenter.service;

import com.example.gamecenter.entity.User;
import com.example.gamecenter.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashedpassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus("normal");
        
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    void testGetById() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        User found = userService.getById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        User found = userService.getById(999L);

        assertNull(found);
    }

    @Test
    void testSave() {
        when(userMapper.insert(any(User.class))).thenReturn(1);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPasswordHash("password");

        boolean result = userService.save(newUser);

        assertTrue(result);
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testSaveFailed() {
        when(userMapper.insert(any(User.class))).thenReturn(0);

        User newUser = new User();
        newUser.setUsername("newuser");

        boolean result = userService.save(newUser);

        assertFalse(result);
    }

    @Test
    void testUpdateById() {
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        testUser.setEmail("newemail@example.com");
        boolean result = userService.updateById(testUser);

        assertTrue(result);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testRemoveById() {
        when(userMapper.deleteById(1L)).thenReturn(1);

        boolean result = userService.removeById(1L);

        assertTrue(result);
        verify(userMapper, times(1)).deleteById(1L);
    }

    @Test
    void testRemoveByIdFailed() {
        when(userMapper.deleteById(999L)).thenReturn(0);

        boolean result = userService.removeById(999L);

        assertFalse(result);
    }
}
