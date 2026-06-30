"""
配置加载器 - 从 auto_test.config 加载统一配置
"""

import os
import json
import configparser
from typing import Any, Dict, List, Optional


class ConfigLoader:
    """配置加载器，支持 dot-style 属性访问"""

    def __init__(self, config_path: str = None):
        if config_path is None:
            config_path = os.path.join(
                os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                "config", "auto_test.config"
            )
        self.config_path = config_path
        self._config = configparser.ConfigParser()
        self._config.read(config_path, encoding="utf-8")
        self._parse_config()

    def _parse_config(self):
        """解析所有配置项到扁平属性"""
        # 系统配置 - 支持环境变量覆盖
        self.base_url = self._get_env_or_config("TEST_BASE_URL", "system", "base_url", "http://localhost:8080")
        # 也检查 CNB 环境变量
        if self.base_url == "http://localhost:8080":
            for env_key in ("CNB_PUBLIC_URL", "PUBLIC_URL", "APP_URL", "WORKSPACE_URL"):
                env_val = os.environ.get(env_key, "")
                if env_val:
                    self.base_url = env_val.rstrip("/")
                    break

        self.api_prefix = self._get_str("system", "api_prefix", "/api")
        self.timeout = self._get_int("system", "timeout", 30)
        self.test_interval_ms = self._get_int("system", "test_interval_ms", 500)

        # 管理员账号
        self.admin_username = self._get_str("admin", "username", "admin")
        self.admin_password = self._get_str("admin", "password", "Admin@123!")

        # AI 配置
        self.ai_api_url = self._get_str("ai", "api_url", "")
        self.ai_api_key = self._get_str("ai", "api_key", "")
        self.ai_models = self._get_list("ai", "models", [])
        self.ai_timeout = self._get_int("ai", "ai_timeout", 120)
        self.ai_test_prompt = self._get_str(
            "ai", "ai_test_prompt",
            '请用中文回复"你好，自动化测试通过"，不要回复其他内容。'
        )

        # 测试范围
        self.test_public_pages = self._get_bool("test_scope", "test_public_pages", True)
        self.test_authenticated_pages = self._get_bool("test_scope", "test_authenticated_pages", True)
        self.test_page_features = self._get_bool("test_scope", "test_page_features", True)
        self.test_rbac = self._get_bool("test_scope", "test_rbac", True)
        self.test_ai_features = self._get_bool("test_scope", "test_ai_features", True)
        self.test_security_features = self._get_bool("test_scope", "test_security_features", True)
        self.test_registration = self._get_bool("test_scope", "test_registration", True)

        # 测试账号
        self.auto_create_accounts = self._get_bool("test_accounts", "auto_create_accounts", True)
        self.username_prefix = self._get_str("test_accounts", "username_prefix", "autotest_")
        self.default_password = self._get_str("test_accounts", "default_password", "Test@123456")
        self.cleanup_after_test = self._get_bool("test_accounts", "cleanup_after_test", True)

        # 邮箱配置
        self.email_smtp_host = self._get_str("email", "smtp_host", "")
        self.email_smtp_port = self._get_int("email", "smtp_port", 465)
        self.email_from = self._get_str("email", "from_email", "")
        self.email_auth_code = self._get_str("email", "auth_code", "")
        self.email_use_ssl = self._get_bool("email", "use_ssl", True)

        # 报告配置
        self.output_dir = self._get_str("report", "output_dir", "./reports")
        self.report_formats = self._get_list("report", "formats", ["json", "html", "markdown"])
        self.verbose = self._get_bool("report", "verbose", True)
        self.console_progress = self._get_bool("report", "console_progress", True)

        # 计算完整 API 基础 URL
        self.api_base_url = f"{self.base_url.rstrip('/')}{self.api_prefix}"

    def _get_str(self, section: str, key: str, default: str = "") -> str:
        try:
            return self._config.get(section, key)
        except (configparser.NoSectionError, configparser.NoOptionError):
            return default

    def _get_int(self, section: str, key: str, default: int = 0) -> int:
        try:
            return self._config.getint(section, key)
        except (configparser.NoSectionError, configparser.NoOptionError, ValueError):
            return default

    def _get_bool(self, section: str, key: str, default: bool = False) -> bool:
        try:
            return self._config.getboolean(section, key)
        except (configparser.NoSectionError, configparser.NoOptionError, ValueError):
            return default

    def _get_list(self, section: str, key: str, default: List = None) -> List:
        if default is None:
            default = []
        try:
            raw = self._config.get(section, key)
            # 尝试 JSON 解析
            return json.loads(raw)
        except (configparser.NoSectionError, configparser.NoOptionError, json.JSONDecodeError):
            # 尝试逗号分隔
            try:
                raw = self._config.get(section, key)
                return [item.strip().strip('"').strip("'") for item in raw.split(",")]
            except:
                return default

    def _get_env_or_config(self, env_key: str, section: str, key: str, default: str = "") -> str:
        """优先从环境变量获取，否则从配置文件获取"""
        env_val = os.environ.get(env_key, "")
        if env_val:
            return env_val.rstrip("/")
        return self._get_str(section, key, default)

    def get(self, section: str, key: str, default: Any = None) -> Any:
        """通用 get 方法"""
        try:
            return self._config.get(section, key)
        except:
            return default

    def validate(self) -> List[str]:
        """验证配置，返回警告列表"""
        warnings = []
        if not self.base_url:
            warnings.append("system.base_url 未配置")
        if not self.admin_password:
            warnings.append("admin.password 未配置，可能无法登录")
        if self.test_ai_features and not self.ai_api_key:
            warnings.append("AI 测试已启用但 ai.api_key 未配置")
        if self.test_ai_features and not self.ai_models:
            warnings.append("AI 测试已启用但 ai.models 为空")
        return warnings

    def __repr__(self):
        return f"<ConfigLoader base_url={self.base_url}>"
