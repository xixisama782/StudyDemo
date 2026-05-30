package com.example.gamecenter.service;

import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.service.impl.FavoriteServiceImpl;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Test
    void checkFavoritesBatch_emptyInput_returnsEmptyList() {
        Result<Object> r1 = favoriteService.checkFavoritesBatch(1L, null);
        Result<Object> r2 = favoriteService.checkFavoritesBatch(1L, Collections.emptyList());

        assertEquals(200, r1.getCode());
        assertEquals(200, r2.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> d1 = (Map<String, Object>) r1.getData();
        @SuppressWarnings("unchecked")
        Map<String, Object> d2 = (Map<String, Object>) r2.getData();
        assertTrue(((List<?>) d1.get("favoritedGameIds")).isEmpty());
        assertTrue(((List<?>) d2.get("favoritedGameIds")).isEmpty());
        verify(favoriteMapper, never()).selectFavoritedGameIds(anyLong(), anyList());
    }

    @Test
    void checkFavoritesBatch_delegatesToMapper() {
        when(favoriteMapper.selectFavoritedGameIds(1L, Arrays.asList(10L, 20L)))
                .thenReturn(Arrays.asList(10L));

        Result<Object> res = favoriteService.checkFavoritesBatch(1L, Arrays.asList(10L, 20L, 10L));

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) data.get("favoritedGameIds");
        assertEquals(1, ids.size());
        assertEquals(10L, ids.get(0));
        verify(favoriteMapper).selectFavoritedGameIds(eq(1L), eq(Arrays.asList(10L, 20L)));
    }

    @Test
    void checkFavoritesBatch_filtersNonPositiveIds() {
        when(favoriteMapper.selectFavoritedGameIds(1L, Collections.singletonList(5L)))
                .thenReturn(Collections.emptyList());

        Result<Object> res = favoriteService.checkFavoritesBatch(1L, Arrays.asList(-1L, 0L, 5L));

        verify(favoriteMapper).selectFavoritedGameIds(eq(1L), eq(Collections.singletonList(5L)));
        assertEquals(200, res.getCode());
    }
}
