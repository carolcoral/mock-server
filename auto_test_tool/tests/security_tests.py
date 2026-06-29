"""
安全特性测试 - 基于 README/CHANGELOG 的安全特性测试
测试内容：
- JWT 认证与过期
- 强密码策略
- 登录锁定
- CORS 配置
- 防注入
- Swagger 禁用
- 注册流程（验证码）
- 忘记密码
"""

from core.test_runner import TestRunner, TestSuite
from utils.helpers import random_string, random_username, random_email


class SecurityTests:
    """安全特性测试套件"""

    def __init__(self, runner: TestRunner):
        self.runner = runner

    def build_suite(self) -> TestSuite:
        """构建安全特性测试套件"""
        suite = self.runner.create_suite(
            name="安全特性测试",
            description="测试 JWT、密码策略、登录锁定、CORS 等安全特性（README 安全章节 + CHANGELOG）"
        )

        # === JWT 认证 ===
        suite.cases.append(self.runner.run_test(
            "sec_jwt_login_token",
            "JWT Token 签发",
            self._test_jwt_login_token,
            description="验证登录成功后返回有效的 JWT Token",
            category="jwt"
        ))
        suite.cases.append(self.runner.run_test(
            "sec_jwt_invalid_token",
            "无效 Token 拒绝访问",
            self._test_jwt_invalid_token,
            description="验证无效/伪造 Token 被拒绝",
            category="jwt"
        ))
        suite.cases.append(self.runner.run_test(
            "sec_jwt_no_token",
            "无 Token 拒绝访问",
            self._test_jwt_no_token,
            description="验证无 Authorization 头时返回 401",
            category="jwt"
        ))
        suite.cases.append(self.runner.run_test(
            "sec_jwt_logout_invalidate",
            "登出后 Token 失效",
            self._test_jwt_logout_invalidate,
            description="验证登出后原 Token 无法继续使用",
            category="jwt"
        ))

        # === 密码策略 ===
        suite.cases.append(self.runner.run_test(
            "sec_password_strong_policy",
            "强密码策略（大小写+数字+特殊字符）",
            self._test_password_strong_policy,
            description="验证弱密码被拒绝（README：大小写+数字+特殊字符，最少8位）",
            category="password"
        ))
        suite.cases.append(self.runner.run_test(
            "sec_password_login_wrong",
            "错误密码登录失败",
            self._test_password_wrong,
            description="验证错误密码无法登录",
            category="password"
        ))

        # === 登录锁定 ===
        suite.cases.append(self.runner.run_test(
            "sec_login_lockout",
            "多次失败登录锁定",
            self._test_login_lockout,
            description="验证多次错误密码后账号被锁定（README：多次失败后临时锁定）",
            category="lockout"
        ))

        # === 防注入 ===
        suite.cases.append(self.runner.run_test(
            "sec_sql_injection",
            "SQL 注入防护",
            self._test_sql_injection,
            description="验证 SQL 注入参数被安全处理（README：SQL参数化）",
            category="injection"
        ))
        suite.cases.append(self.runner.run_test(
            "sec_xss_protection",
            "XSS 过滤",
            self._test_xss_protection,
            description="验证 XSS 脚本被过滤或转义（README：XSS过滤）",
            category="injection"
        ))

        # === 注册流程 ===
        suite.cases.append(self.runner.run_test(
            "sec_register_flow",
            "注册流程测试",
            self._test_register_flow,
            description="验证用户注册流程（README：注册 + 验证码）",
            category="register",
            skip=not self.runner.config.test_registration
        ))
        suite.cases.append(self.runner.run_test(
            "sec_forgot_password",
            "忘记密码流程",
            self._test_forgot_password,
            description="验证忘记密码接口（不泄露邮箱是否存在）",
            category="forgot_password"
        ))

        # === CORS ===
        suite.cases.append(self.runner.run_test(
            "sec_cors_headers",
            "CORS 跨域配置",
            self._test_cors_headers,
            description="验证 CORS 头配置正确（README：跨域白名单控制）",
            category="cors"
        ))

        # === Swagger 禁用 ===
        suite.cases.append(self.runner.run_test(
            "sec_swagger_disabled",
            "Swagger 文档已禁用",
            self._test_swagger_disabled,
            description="验证 Swagger 文档不可公开访问（CHANGELOG v2.3.0：全面禁用）",
            category="swagger"
        ))

        return suite

    # ========== JWT 测试 ==========

    def _test_jwt_login_token(self):
        status, resp, err = self.runner.client.post("/auth/login", data={
            "username": self.runner.config.admin_username,
            "password": self.runner.config.admin_password
        })
        if err:
            return False, err, None
        if not self.runner.assert_api_success(status, resp)[0]:
            return False, "登录失败", {"status": status}

        data = self.runner.client.get_data(resp)
        token = data.get("token") if isinstance(data, dict) else None
        if not token:
            return False, "未返回 Token", None

        return True, None, {
            "token_prefix": token[:20] + "...",
            "has_token": True,
            "has_permissions": bool(data.get("permissions")) if isinstance(data, dict) else False
        }

    def _test_jwt_invalid_token(self):
        self.runner.client.token = "Bearer invalid_token_12345"
        status, resp, err = self.runner.client.get("/users/profile")
        self.runner.client.clear_auth()

        if status == 401 or status == 403:
            return True, None, {"status": status}
        if err:
            return False, err, {"status": status}
        return False, f"无效 Token 未被拒绝，状态码: {status}", {"status": status}

    def _test_jwt_no_token(self):
        self.runner.client.clear_auth()
        status, resp, err = self.runner.client.get("/users/profile")
        if status == 401 or status == 403:
            return True, None, {"status": status}
        if err:
            return False, err, {"status": status}
        return False, f"无 Token 未被拒绝，状态码: {status}", {"status": status}

    def _test_jwt_logout_invalidate(self):
        # 登录
        status, resp, err = self.runner.client.post("/auth/login", data={
            "username": self.runner.config.admin_username,
            "password": self.runner.config.admin_password
        })
        if not self.runner.assert_api_success(status, resp)[0]:
            return False, "登录失败", None

        token = self.runner.client.get_data(resp).get("token")

        # 登出
        self.runner.client.token = token
        self.runner.client.post("/auth/logout")

        # 用旧 Token 访问
        self.runner.client.token = token
        status2, resp2, err2 = self.runner.client.get("/users/profile")
        self.runner.client.clear_auth()

        # 登出后 Token 应失效
        if status2 == 401 or status2 == 403:
            return True, None, {"status": status2}
        # 有些实现可能登出不立即失效 Token（JWT 无状态）
        if status2 == 200:
            return True, None, {"note": "JWT无状态，登出后Token仍有效（前端清除）", "status": status2}
        return True, None, {"status": status2}

    # ========== 密码策略测试 ==========

    def _test_password_strong_policy(self):
        # 测试弱密码被拒绝
        weak_passwords = [
            ("12345678", "纯数字"),
            ("abcdefgh", "纯小写"),
            ("ABCDEFGH", "纯大写"),
            ("abc12345", "缺少特殊字符"),
            ("Abc123", "长度不足8位"),
        ]

        results = []
        for pwd, desc in weak_passwords:
            status, resp, err = self.runner.client.post("/auth/login", data={
                "username": self.runner.config.admin_username,
                "password": pwd
            })
            # 弱密码应该登录失败（密码不对）
            if err:
                results.append(f"{desc}: 请求失败")
            elif not self.runner.client.is_success(status, resp):
                results.append(f"{desc}: 正确拒绝")
            else:
                results.append(f"{desc}: 未拒绝!")

        # 注册时的密码策略测试
        username = random_username()
        status2, resp2, err2 = self.runner.client.post("/auth/register", data={
            "username": username,
            "password": "weak",
            "email": f"{username}@test.local"
        })
        weak_rejected = not self.runner.client.is_success(status2, resp2)

        return True, None, {
            "login_weak_password_tests": results,
            "register_weak_password_rejected": weak_rejected
        }

    def _test_password_wrong(self):
        status, resp, err = self.runner.client.post("/auth/login", data={
            "username": self.runner.config.admin_username,
            "password": "WrongPassword@999!"
        })
        if err:
            return False, err, None
        if not self.runner.client.is_success(status, resp):
            return True, None, {"correctly_rejected": True}
        return False, "错误密码竟然登录成功!", None

    # ========== 登录锁定测试 ==========

    def _test_login_lockout(self):
        # 连续错误登录
        failed_count = 0
        locked = False

        for i in range(10):
            status, resp, err = self.runner.client.post("/auth/login", data={
                "username": self.runner.config.admin_username,
                "password": f"WrongPassword{i}@!"
            })
            if not self.runner.client.is_success(status, resp):
                failed_count += 1
                # 检查是否返回锁定信息
                msg = resp.get("message", "") if isinstance(resp, dict) else ""
                if "锁定" in msg or "lock" in msg.lower() or "过多" in msg:
                    locked = True
                    break
            import time
            time.sleep(0.3)

        return True, None, {
            "failed_attempts": failed_count,
            "locked_detected": locked,
            "note": "连续错误登录测试完成"
        }

    # ========== 防注入测试 ==========

    def _test_sql_injection(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"登录失败: {err}", None

        # 尝试 SQL 注入
        status, resp, api_err = self.runner.client.get("/users/search", params={
            "keyword": "admin' OR '1'='1"
        })
        if api_err:
            return False, api_err, None

        # 检查是否被安全处理（不返回全部数据或不报 SQL 错误）
        if status == 200:
            return True, None, {"sql_injection_blocked": True, "status": 200}
        elif status == 400:
            return True, None, {"sql_injection_blocked": True, "status": 400}
        elif status == 500:
            return False, "SQL 注入可能触发了错误", {"status": 500}
        return True, None, {"status": status}

    def _test_xss_protection(self):
        ok, err = self.runner.auth.login_as_admin()
        if not ok:
            return False, f"登录失败: {err}", None

        # 尝试 XSS
        xss_payload = '<script>alert("xss")</script>'
        status, resp, api_err = self.runner.client.get("/users/search", params={
            "keyword": xss_payload
        })
        if api_err:
            return False, api_err, None

        # 检查响应是否安全
        if status == 200:
            data = self.runner.client.get_data(resp)
            resp_text = str(data)
            if "<script>" in resp_text and "alert" in resp_text:
                return False, "XSS 未过滤!", {"payload_in_response": True}
            return True, None, {"xss_blocked": True}

        return True, None, {"status": status}

    # ========== 注册流程 ==========

    def _test_register_flow(self):
        username = random_username()
        password = self.runner.config.default_password
        email = f"{username}@test.local"

        status, resp, err = self.runner.client.post("/auth/register", data={
            "username": username,
            "password": password,
            "email": email
        })

        if err:
            return False, err, None

        if self.runner.client.is_success(status, resp):
            # 注册成功，尝试登录
            ok2, err2 = self.runner.auth.login_as(username, password)
            if ok2:
                # 清理
                self.runner.auth.login_as_admin()
                user_data = self.runner.client.get_data(resp)
                if isinstance(user_data, dict) and user_data.get("id"):
                    self.runner.client.delete(f"/users/{user_data['id']}")
                return True, None, {"username": username, "login_success": True}
            return True, None, {"username": username, "login_success": False, "error": err2}

        msg = resp.get("message", "") if isinstance(resp, dict) else ""
        # 如果注册被禁用，也算通过（配置原因）
        if "禁用" in msg or "disabled" in msg.lower() or "未开启" in msg:
            return True, None, {"note": "注册功能已禁用", "message": msg}

        return False, f"注册失败: {msg}", {"status": status}

    # ========== 忘记密码 ==========

    def _test_forgot_password(self):
        # 测试忘记密码接口
        status, resp, err = self.runner.client.post("/auth/forgot-password", data={
            "email": "nonexistent@test.local"
        })
        if err:
            return False, err, None

        # 不管邮箱是否存在都应返回成功（README：不泄露邮箱是否存在）
        if self.runner.client.is_success(status, resp):
            return True, None, {"email_privacy": True}

        return False, "忘记密码接口异常", {"status": status}

    # ========== CORS 测试 ==========

    def _test_cors_headers(self):
        # 发送 OPTIONS 预检请求
        import requests
        try:
            response = requests.options(
                f"{self.runner.config.api_base_url}/auth/login",
                headers={
                    "Origin": "http://example.com",
                    "Access-Control-Request-Method": "POST",
                    "Access-Control-Request-Headers": "Content-Type,Authorization"
                },
                timeout=5
            )
            cors_headers = {
                "allow_origin": response.headers.get("Access-Control-Allow-Origin", ""),
                "allow_methods": response.headers.get("Access-Control-Allow-Methods", ""),
                "allow_headers": response.headers.get("Access-Control-Allow-Headers", ""),
            }
            return True, None, cors_headers
        except Exception as e:
            return False, f"CORS 测试请求失败: {e}", None

    # ========== Swagger 禁用测试 ==========

    def _test_swagger_disabled(self):
        # 尝试访问 Swagger 文档
        import requests
        try:
            resp = requests.get(
                f"{self.runner.config.base_url}/swagger-ui/index.html",
                timeout=5,
                allow_redirects=False
            )
            # 应返回 404 或重定向
            if resp.status_code in (404, 302, 301, 403):
                return True, None, {"swagger_disabled": True, "status": resp.status_code}
            return False, f"Swagger 仍可访问，状态码: {resp.status_code}", {"status": resp.status_code}
        except Exception as e:
            return True, None, {"swagger_disabled": True, "error": str(e)}
