"""
AI 功能测试 - 基于 README/CHANGELOG 的 AI 特性测试
测试内容：
- AI 模型可用性检测与自动切换
- AI 对话功能
- AI 生成功能（响应数据/代码模板/邮件模板/接口描述）
- AI 配置管理
- AI 调用统计
"""

from core.test_runner import TestRunner, TestSuite
from models.ai_model_manager import AIModelManager


class AITests:
    """AI 功能测试套件"""

    def __init__(self, runner: TestRunner, ai_manager: AIModelManager):
        self.runner = runner
        self.ai_manager = ai_manager

    def build_suite(self) -> TestSuite:
        """构建 AI 功能测试套件"""
        suite = self.runner.create_suite(
            name="AI 功能测试",
            description="测试 AI 模型、对话、生成等功能（基于 README/CHANGELOG v2.3.0）"
        )

        # === 模型管理测试 ===
        suite.cases.append(self.runner.run_test(
            "ai_model_detect_all",
            "检测所有 AI 模型可用性",
            self._test_model_detect_all,
            description="启动时检测所有配置模型，记录可用/不可用状态",
            category="ai_model"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_model_auto_switch",
            "模型不可用时自动切换",
            self._test_model_auto_switch,
            description="验证模型不可用时自动切换到下一个可用模型",
            category="ai_model"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_model_all_failed_warning",
            "全部模型不可用时告警",
            self._test_model_all_failed_warning,
            description="验证所有模型不可用时生成告警信息",
            category="ai_model"
        ))

        # === AI 直接 API 调用测试 ===
        suite.cases.append(self.runner.run_test(
            "ai_direct_chat",
            "AI 直接对话测试",
            self._test_direct_chat,
            description="直接调用 AI API 验证对话功能（使用配置的 API）",
            category="ai_chat"
        ))

        # === AI 配置管理 ===
        suite.cases.append(self.runner.run_test(
            "ai_config_list",
            "获取 AI 服务商配置列表",
            self._test_ai_config_list,
            description="GET /api/ai-config（管理员）",
            category="ai_config"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_config_enabled_public",
            "获取已启用的 AI 服务商（公开接口）",
            self._test_ai_config_enabled_public,
            description="GET /api/ai-config/enabled（所有认证用户可访问，CHANGELOG v2.3.0）",
            category="ai_config"
        ))

        # === AI 生成功能（后端 API） ===
        suite.cases.append(self.runner.run_test(
            "ai_generate_response",
            "AI 生成响应数据",
            self._test_ai_generate_response,
            description="POST /api/ai/generate-response（CHANGELOG v2.2.0 AI 智能生成）",
            category="ai_generate"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_generate_description",
            "AI 生成接口描述",
            self._test_ai_generate_description,
            description="POST /api/ai/generate-description",
            category="ai_generate"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_generate_code_template",
            "AI 生成代码模板",
            self._test_ai_generate_code_template,
            description="POST /api/ai/generate-code-template（CHANGELOG v2.2.0 6种转换器）",
            category="ai_generate"
        ))
        suite.cases.append(self.runner.run_test(
            "ai_generate_email_template",
            "AI 生成邮件模板",
            self._test_ai_generate_email_template,
            description="POST /api/ai/generate-email-template（CHANGELOG v2.2.0）",
            category="ai_generate"
        ))

        # === AI 调用统计 ===
        suite.cases.append(self.runner.run_test(
            "ai_call_statistics",
            "AI 调用统计",
            self._test_ai_call_statistics,
            description="验证 AI 调用日志记录和统计功能（CHANGELOG v2.3.0）",
            category="ai_stats"
        ))

        return suite

    # ========== 模型管理测试 ==========

    def _test_model_detect_all(self):
        """检测所有模型可用性"""
        if not self.ai_manager.config.ai_models:
            return False, "未配置 AI 模型列表", None

        results = self.ai_manager.test_all_models()
        available = [r for r in results if r[1]]
        failed = [r for r in results if not r[1]]

        details = {
            "total": len(results),
            "available": len(available),
            "failed": len(failed),
            "models": [{"model": r[0], "available": r[1], "error": r[2]} for r in results]
        }

        if len(available) == 0:
            return False, "所有模型均不可用！", details

        return True, None, details

    def _test_model_auto_switch(self):
        """测试模型自动切换"""
        if not self.ai_manager.is_available():
            return True, None, {"note": "无可用模型，跳过切换测试"}

        # 记录当前模型
        current = self.ai_manager.get_current_model()

        # 手动标记当前模型为不可用并切换
        if current and current in self.ai_manager.available_models:
            self.ai_manager.available_models.remove(current)
            self.ai_manager.failed_models.append(current)
            self.ai_manager.current_model = None

        # 尝试获取下一个模型
        next_model = self.ai_manager.get_current_model()
        switched = next_model is not None and next_model != current

        # 恢复
        if current:
            if current not in self.ai_manager.available_models:
                self.ai_manager.available_models.insert(0, current)
            if current in self.ai_manager.failed_models:
                self.ai_manager.failed_models.remove(current)
            self.ai_manager.current_model = current

        if len(self.ai_manager.available_models) > 1:
            return switched, None if switched else "切换失败", {
                "previous_model": current,
                "next_model": next_model,
                "switched": switched
            }
        else:
            return True, None, {"note": "仅一个可用模型，无需切换"}

    def _test_model_all_failed_warning(self):
        """测试全部不可用告警"""
        status = self.ai_manager.get_status_report()

        if status["all_failed"]:
            return False, status["warning"], status

        # 模拟全部不可用
        backup_available = list(self.ai_manager.available_models)
        backup_failed = list(self.ai_manager.failed_models)
        backup_current = self.ai_manager.current_model

        # 将所有模型标记为失败
        all_models = list(self.ai_manager.config.ai_models)
        self.ai_manager.available_models = []
        self.ai_manager.failed_models = list(all_models)
        self.ai_manager.current_model = None
        self.ai_manager._all_failed_warning = True

        warning = self.ai_manager.get_status_report().get("warning")

        # 恢复
        self.ai_manager.available_models = backup_available
        self.ai_manager.failed_models = backup_failed
        self.ai_manager.current_model = backup_current
        self.ai_manager._all_failed_warning = len(backup_available) == 0

        if warning:
            return True, None, {"warning_generated": True, "warning_text": warning}
        return False, "告警信息未生成", None

    # ========== AI 直接对话测试 ==========

    def _test_direct_chat(self):
        """直接调用 AI API 测试对话"""
        if not self.ai_manager.is_available():
            return False, "无可用 AI 模型", None

        success, content, err = self.ai_manager.send_chat(
            self.runner.config.ai_test_prompt,
            max_tokens=50
        )

        if success:
            return True, None, {
                "model": self.ai_manager.get_current_model(),
                "response": content[:200] if content else "",
                "response_length": len(content) if content else 0
            }
        return False, err, {"model": self.ai_manager.get_current_model()}

    # ========== AI 配置管理测试 ==========

    def _test_ai_config_list(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.get("/ai-config")
        if api_err:
            return False, api_err, None
        passed, msg = self.runner.assert_api_success(status, resp)
        data = self.runner.client.get_data(resp)
        count = len(data) if isinstance(data, list) else "N/A"
        return passed, msg, {"config_count": count}

    def _test_ai_config_enabled_public(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.get("/ai-config/enabled")
        if api_err:
            return False, api_err, None
        return self.runner.assert_api_success(status, resp)

    # ========== AI 生成功能测试 ==========

    def _test_ai_generate_response(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.post("/ai/generate-response", data={
            "apiMethod": "GET",
            "apiPath": "/api/test/users",
            "apiName": "获取用户列表",
            "description": "返回用户列表数据",
            "count": 2
        })
        if api_err:
            return False, api_err, None
        passed, msg = self.runner.assert_api_success(status, resp)
        if passed:
            data = self.runner.client.get_data(resp)
            return True, None, {"generated": str(data)[:200] if data else "N/A"}
        return passed, msg, None

    def _test_ai_generate_description(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.post("/ai/generate-description", data={
            "apiMethod": "POST",
            "apiPath": "/api/users",
            "apiName": "创建用户"
        })
        if api_err:
            return False, api_err, None
        return self.runner.assert_api_success(status, resp)

    def _test_ai_generate_code_template(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.post("/ai/generate-code-template", data={
            "apiMethod": "GET",
            "apiPath": "/api/test/data",
            "apiName": "获取数据",
            "description": "返回JSON数据",
            "transformerType": "RESPONSE_WRAPPER"
        })
        if api_err:
            return False, api_err, None
        return self.runner.assert_api_success(status, resp)

    def _test_ai_generate_email_template(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        status, resp, api_err = self.runner.client.post("/ai/generate-email-template", data={
            "templateType": "GENERAL",
            "templateName": "测试邮件"
        })
        if api_err:
            return False, api_err, None
        return self.runner.assert_api_success(status, resp)

    # ========== AI 调用统计测试 ==========

    def _test_ai_call_statistics(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", None

        # 测试统计接口（按日查询）
        status, resp, api_err = self.runner.client.get(
            "/statistics/ai-calls",
            params={"type": "daily"}
        )
        if api_err:
            return False, api_err, None
        return self.runner.assert_api_success(status, resp)
