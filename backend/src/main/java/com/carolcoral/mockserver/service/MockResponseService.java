/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.repository.MockResponseRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Mock响应服务类
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Service
public class MockResponseService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MockResponseService.class);

    /**
     * 构造器
     */
    public MockResponseService(MockResponseRepository mockResponseRepository) {
        this.mockResponseRepository = mockResponseRepository;
    }

    private final MockResponseRepository mockResponseRepository;

    /**
     * 根据ID获取响应
     *
     * @param id 响应ID
     * @return 响应实体
     */
    public MockResponse getById(Long id) {
        Optional<MockResponse> opt = mockResponseRepository.findById(id);
        return opt.orElse(null);
    }
}
