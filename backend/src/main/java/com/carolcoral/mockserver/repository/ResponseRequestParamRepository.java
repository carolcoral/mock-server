/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.repository;

import com.carolcoral.mockserver.entity.ResponseRequestParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 响应请求参数Repository接口
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Repository
public interface ResponseRequestParamRepository extends JpaRepository<ResponseRequestParam, Long> {

}
