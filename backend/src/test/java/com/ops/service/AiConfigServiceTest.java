package com.ops.service;

import com.ops.entity.AiConfig;
import com.ops.mapper.AiConfigMapper;
import com.ops.service.impl.AiConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiConfigServiceTest {

    @Mock
    private AiConfigMapper aiConfigMapper;

    @InjectMocks
    private AiConfigServiceImpl aiConfigService;

    private AiConfig aiConfig;

    @BeforeEach
    void setUp() {
        aiConfig = new AiConfig();
        aiConfig.setId(1L);
        aiConfig.setName("OpenAI API");
        aiConfig.setProvider("openai");
        aiConfig.setApiKey("sk-test");
        aiConfig.setModel("gpt-4o");
        aiConfig.setIsDefault(true);
        aiConfig.setStatus("ACTIVE");
    }

    @Test
    void testList() {
        when(aiConfigMapper.selectList(any())).thenReturn(Arrays.asList(aiConfig));

        List<AiConfig> result = aiConfigService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OpenAI API", result.get(0).getName());
    }

    @Test
    void testGetById() {
        when(aiConfigMapper.selectById(1L)).thenReturn(aiConfig);

        AiConfig result = aiConfigService.getById(1L);

        assertNotNull(result);
        assertEquals("openai", result.getProvider());
    }

    @Test
    void testCreate_SetAsDefault() {
        aiConfig.setIsDefault(true);
        when(aiConfigMapper.insert(any(AiConfig.class))).thenReturn(1);

        AiConfig result = aiConfigService.create(aiConfig);

        assertNotNull(result);
        verify(aiConfigMapper).insert(any(AiConfig.class));
    }

    @Test
    void testUpdate() {
        when(aiConfigMapper.selectById(1L)).thenReturn(aiConfig);
        when(aiConfigMapper.updateById(any(AiConfig.class))).thenReturn(1);

        aiConfig.setName("Updated API");
        AiConfig result = aiConfigService.update(1L, aiConfig);

        assertNotNull(result);
        verify(aiConfigMapper).updateById(any(AiConfig.class));
    }

    @Test
    void testDelete() {
        when(aiConfigMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> aiConfigService.delete(1L));
        verify(aiConfigMapper).deleteById(1L);
    }

    @Test
    void testGetDefault() {
        when(aiConfigMapper.selectOne(any())).thenReturn(aiConfig);

        AiConfig result = aiConfigService.getDefault();

        assertNotNull(result);
        assertTrue(result.getIsDefault());
    }

    @Test
    void testSetDefault() {
        when(aiConfigMapper.updateById(any(AiConfig.class))).thenReturn(1);

        assertDoesNotThrow(() -> aiConfigService.setDefault(1L));
        verify(aiConfigMapper).updateById(any(AiConfig.class));
    }
}
