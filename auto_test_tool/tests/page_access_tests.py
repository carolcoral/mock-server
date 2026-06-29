"""
页面访问测试 - 测试所有前端页面的可访问性
"""

from core.test_runner import TestRunner, TestSuite


class PageAccessTests:
    """页面访问测试套件"""

    # 所有路由定义（来自 frontend/src/router/index.js）
    ALL_ROUTES = [
        # 公开页面
        {"path": "/", "name": "欢迎页 (Welcome)", "auth": False, "perm": None},
        {"path": "/changelog", "name": "变更日志 (Changelog)", "auth": False, "perm": None},
        {"path": "/login", "name": "登录页 (Login)", "auth": False, "perm": None},
        {"path": "/register", "name": "注册页 (Register)", "auth": False, "perm": None},
        {"path": "/forgot-password", "name": "忘记密码 (ForgotPassword)", "auth": False, "perm": None},
        {"path": "/guide", "name": "使用引导 (Guide)", "auth": False, "perm": None},
        # 需认证页面
        {"path": "/dashboard", "name": "仪表盘 (Home)", "auth": True, "perm": "dashboard:view"},
        {"path": "/projects", "name": "项目管理 (Projects)", "auth": True, "perm": "project:view"},
        {"path": "/apis", "name": "接口管理 (Apis)", "auth": True, "perm": "api:view"},
        {"path": "/code-templates", "name": "代码模板 (CodeTemplates)", "auth": True, "perm": "code-template:view"},
        {"path": "/ai-chat", "name": "AI对话 (AiChat)", "auth": True, "perm": "ai-chat:view"},
        {"path": "/statistics", "name": "数据统计 (Statistics)", "auth": True, "perm": "statistics:view"},
        {"path": "/users", "name": "用户管理 (Users)", "auth": True, "perm": "user:view"},
        {"path": "/roles", "name": "角色管理 (Roles)", "auth": True, "perm": "role:view"},
        {"path": "/permissions", "name": "权限管理 (Permissions)", "auth": True, "perm": "permission:view"},
        {"path": "/email-templates", "name": "邮件模板 (EmailTemplates)", "auth": True, "perm": "email-template:view"},
        {"path": "/ai-settings", "name": "AI设置 (AiSettings)", "auth": True, "perm": "ai-settings:view"},
        {"path": "/settings", "name": "系统设置 (Settings)", "auth": True, "perm": "settings:view"},
        {"path": "/profile", "name": "个人信息 (Profile)", "auth": True, "perm": None},
    ]

    def __init__(self, runner: TestRunner):
        self.runner = runner

    def build_suite(self) -> TestSuite:
        """构建页面访问测试套件"""
        suite = self.runner.create_suite(
            name="页面访问测试",
            description="测试所有前端页面的 HTTP 可访问性"
        )

        for route in self.ALL_ROUTES:
            path = route["path"]
            name = route["name"]
            requires_auth = route["auth"]
            required_perm = route["perm"]

            if requires_auth and not self.runner.config.test_authenticated_pages:
                suite.cases.append(self.runner.run_test(
                    f"page_{path.replace('/', '_')}",
                    f"访问 {name}",
                    lambda: (True, None, None),
                    description=f"路径: {path}",
                    category="page_access",
                    skip=True
                ))
                continue

            if not requires_auth and not self.runner.config.test_public_pages:
                suite.cases.append(self.runner.run_test(
                    f"page_{path.replace('/', '_')}",
                    f"访问 {name}",
                    lambda: (True, None, None),
                    description=f"路径: {path}",
                    category="page_access",
                    skip=True
                ))
                continue

            if requires_auth:
                suite.cases.append(self.runner.run_test(
                    f"page_{path.replace('/', '_')}",
                    f"访问 {name}",
                    lambda p=path, n=name, rp=required_perm: self._test_authenticated_page(p, n, rp),
                    description=f"路径: {path}" + (f" (需权限: {required_perm})" if required_perm else ""),
                    category="page_access"
                ))
            else:
                suite.cases.append(self.runner.run_test(
                    f"page_{path.replace('/', '_')}",
                    f"访问 {name}",
                    lambda p=path, n=name: self._test_public_page(p, n),
                    description=f"路径: {path}",
                    category="page_access"
                ))

        return suite

    def _test_public_page(self, path: str, name: str):
        """测试公开页面"""
        # 清除认证
        self.runner.client.clear_auth()
        status, err = self.runner.client.visit_page(path)
        if err:
            return False, f"访问失败: {err}", {"path": path, "status": status}
        if status != 200:
            return False, f"状态码 {status} != 200", {"path": path, "status": status}
        return True, None, {"path": path, "status": status}

    def _test_authenticated_page(self, path: str, name: str, required_perm: str):
        """测试需认证页面"""
        # 确保已登录为管理员
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"管理员登录失败: {err}", {"path": path}

        status, page_err = self.runner.client.visit_page(path)
        if page_err:
            return False, f"访问失败: {page_err}", {"path": path, "status": status}

        if status == 200:
            return True, None, {"path": path, "status": status, "permission": required_perm}
        elif status == 403:
            return False, f"403 禁止访问（权限不足）", {"path": path, "status": status, "permission": required_perm}
        else:
            return False, f"状态码 {status} != 200", {"path": path, "status": status}
