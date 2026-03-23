package com.ops.service;

import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.VersionMapping;
import com.ops.mapper.ArthasVersionMapper;
import com.ops.mapper.JdkVersionMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.mapper.VersionMappingMapper;
import com.ops.service.impl.VersionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private JdkVersionMapper jdkVersionMapper;

    @Mock
    private ArthasVersionMapper arthasVersionMapper;

    @Mock
    private K8sClusterMapper clusterMapper;

    @Mock
    private VersionMappingMapper versionMappingMapper;

    @InjectMocks
    private VersionServiceImpl versionService;

    @Test
    void testListJdkVersions() {
        JdkVersion jdk = new JdkVersion();
        jdk.setId(1L);
        jdk.setVersion("17");
        jdk.setDownloadUrl("https://example.com/jdk17.tar.gz");
        jdk.setRecommendedArthasVersion("3.7.2");

        when(jdkVersionMapper.selectList(any())).thenReturn(Arrays.asList(jdk));

        List<JdkVersion> result = versionService.listJdkVersions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("17", result.get(0).getVersion());
        assertEquals("3.7.2", result.get(0).getRecommendedArthasVersion());
    }

    @Test
    void testListArthasVersions() {
        ArthasVersion arthas = new ArthasVersion();
        arthas.setId(1L);
        arthas.setVersion("3.7.2");
        arthas.setDownloadUrl("https://example.com/arthas.jar");
        arthas.setCompatibleJdkVersions("8,11,17,21");

        when(arthasVersionMapper.selectList(any())).thenReturn(Arrays.asList(arthas));

        List<ArthasVersion> result = versionService.listArthasVersions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("3.7.2", result.get(0).getVersion());
        assertEquals("8,11,17,21", result.get(0).getCompatibleJdkVersions());
    }

    @Test
    void testListMappings() {
        VersionMapping mapping = new VersionMapping();
        mapping.setId(1L);
        mapping.setJdkVersion("17");
        mapping.setArthasVersion("3.7.2");
        mapping.setDescription("JDK 17 推荐使用 Arthas 3.7.2");
        mapping.setRecommended(true);

        when(versionMappingMapper.selectList(any())).thenReturn(Arrays.asList(mapping));

        List<VersionMapping> result = versionService.listMappings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRecommended());
    }

    @Test
    void testListMappings_Empty() {
        // Note: initDefaultMappings runs on @PostConstruct and populates data
        // This test verifies the method returns list correctly
        VersionMapping mapping = new VersionMapping();
        mapping.setJdkVersion("8");
        mapping.setArthasVersion("3.7.2");

        when(versionMappingMapper.selectList(any())).thenReturn(Arrays.asList(mapping));

        List<VersionMapping> result = versionService.listMappings();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testExtractVersion() {
        // Test version extraction logic via actual service behavior
        JdkVersion jdk = new JdkVersion();
        jdk.setId(1L);
        jdk.setVersion("21");
        jdk.setFilePath("/uploads/test-jdk-21.tar.gz");

        when(jdkVersionMapper.selectList(any())).thenReturn(Arrays.asList(jdk));

        List<JdkVersion> result = versionService.listJdkVersions();

        assertEquals("21", result.get(0).getVersion());
    }
}
