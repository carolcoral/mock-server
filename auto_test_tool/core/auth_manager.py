"""
认证管理器 - 处理登录、登出、Token 管理、测试账号创建/清理
"""

import time
import random
import string
from typing import Optional, Dict, List, Tuple
from .http_client import HttpClient


class AuthManager:
    """认证管理器"""

    def __init__(self, client: HttpClient, config):
        self.client = client
        self.config = config
        self._created_users: List[Dict] = []  # 记录自动创建的测试账号
        self._admin_info: Optional[Dict] = None

    # ---------- 管理员登录 ----------

    def login_as_admin(self) -> Tuple[bool, Optional[str]]:
        """
        以管理员身份登录
        返回: (是否成功, 错误信息)
        """
        status, resp, err = self.client.post("/auth/login", data={
            "username": self.config.admin_username,
            "password": self.config.admin_password
        })

        if err:
            return False, err

        if self.client.is_success(status, resp):
            data = self.client.get_data(resp)
            self.client.token = data.get("token")
            self.client.current_user = {
                "id": data.get("userId"),
                "username": data.get("username"),
                "role": data.get("role"),
                "email": data.get("email"),
                "permissions": data.get("permissions", [])
            }
            self._admin_info = self.client.current_user.copy()
            return True, None

        msg = resp.get("message", "登录失败") if isinstance(resp, dict) else "登录失败"
        return False, msg

    def logout(self):
        """登出当前用户"""
        self.client.post("/auth/logout")
        self.client.clear_auth()

    # ---------- 用户登录 ----------

    def login_as(self, username: str, password: str) -> Tuple[bool, Optional[str]]:
        """
        以指定用户身份登录
        返回: (是否成功, 错误信息)
        """
        self.client.clear_auth()
        status, resp, err = self.client.post("/auth/login", data={
            "username": username,
            "password": password
        })

        if err:
            return False, err

        if self.client.is_success(status, resp):
            data = self.client.get_data(resp)
            if not isinstance(data, dict):
                return False, f"登录响应格式异常: {type(data).__name__}"
            self.client.token = data.get("token")
            self.client.current_user = {
                "id": data.get("userId"),
                "username": data.get("username"),
                "role": data.get("role"),
                "email": data.get("email"),
                "permissions": data.get("permissions", [])
            }
            return True, None

        msg = resp.get("message", "登录失败") if isinstance(resp, dict) else "登录失败"
        return False, msg

    def login_as_user(self, user: Dict) -> Tuple[bool, Optional[str]]:
        """以指定用户对象登录（使用其 username 和 password）"""
        return self.login_as(user.get("username", ""), user.get("_password", ""))

    # ---------- 测试账号管理 ----------

    def create_test_user(
        self,
        username: str = None,
        password: str = None,
        role_id: int = None
    ) -> Tuple[Optional[Dict], Optional[str]]:
        """
        创建测试账号（需要当前已有管理员权限）
        返回: (用户信息字典, 错误信息)
        """
        if username is None:
            suffix = "".join(random.choices(string.ascii_lowercase + string.digits, k=8))
            username = f"{self.config.username_prefix}{suffix}"
        if password is None:
            password = self.config.default_password

        # 方式1: 通过 /auth/register 注册
        reg_data = {
            "username": username,
            "password": password,
            "email": f"{username}@test.autotest.local"
        }

        status, resp, err = self.client.post("/auth/register", data=reg_data)
        if err:
            return None, f"注册失败: {err}"

        if self.client.is_success(status, resp):
            # 获取创建的用户信息
            user_data = self.client.get_data(resp)
            if isinstance(user_data, dict) and user_data.get("id"):
                user_data["_password"] = password
                if role_id is not None:
                    self._assign_role(user_data.get("id"), role_id)
                self._created_users.append(user_data)
                return user_data, None

        # 方式2: 通过 /users API 创建（管理员权限）
        cr_data = {
            "username": username,
            "password": password,
            "email": f"{username}@test.autotest.local",
            "role": "USER"
        }
        status2, resp2, err2 = self.client.post("/users", data=cr_data)
        if err2 is None and self.client.is_success(status2, resp2):
            user_data = self.client.get_data(resp2)
            if isinstance(user_data, dict) and user_data.get("id"):
                user_data["_password"] = password
                if role_id is not None:
                    self._assign_role(user_data.get("id"), role_id)
                self._created_users.append(user_data)
                return user_data, None

        # 方式3: 通过用户名查找已创建的用户
        status3, resp3, err3 = self.client.get("/users")
        if err3 is None and self.client.is_success(status3, resp3):
            users = self.client.get_data(resp3)
            if isinstance(users, list):
                for u in users:
                    if isinstance(u, dict) and u.get("username") == username:
                        u["_password"] = password
                        if role_id is not None:
                            self._assign_role(u.get("id"), role_id)
                        self._created_users.append(u)
                        return u, None

        return None, "创建用户后无法获取用户信息"

    def _assign_role(self, user_id: int, role_id: int) -> bool:
        """为用户分配角色"""
        status, resp, err = self.client.put(
            f"/users/{user_id}",
            data={"roleId": role_id}
        )
        return err is None and self.client.is_success(status, resp)

    def create_user_with_role(self, role_name: str) -> Tuple[Optional[Dict], Optional[str]]:
        """
        创建用户并分配指定角色
        role_name: 角色名称，如 "普通用户"
        如果角色不存在，则自动创建该角色
        """
        # 先获取角色列表
        status, resp, err = self.client.get("/roles")
        if err or not self.client.is_success(status, resp):
            return None, f"获取角色列表失败: {err}"

        roles = self.client.get_data(resp)
        if not isinstance(roles, list):
            return None, "角色列表格式错误"

        target_role = None
        for role in roles:
            if isinstance(role, dict) and (role.get("name") == role_name or role.get("code") == role_name):
                target_role = role
                break

        if not target_role:
            # 尝试自动创建该角色
            role_code = role_name.upper().replace(" ", "_")
            cr_status, cr_resp, cr_err = self.client.post("/roles", data={
                "name": role_name,
                "code": role_code,
                "description": f"自动化测试自动创建的角色: {role_name}"
            })
            if cr_err or not self.client.is_success(cr_status, cr_resp):
                return None, f"未找到角色且创建失败: {role_name}"
            data = self.client.get_data(cr_resp)
            if isinstance(data, dict):
                target_role = data
            else:
                return None, f"创建角色后无法获取角色信息: {role_name}"

        user, err = self.create_test_user(role_id=target_role.get("id"))
        if err:
            return None, err
        return user, None

    def cleanup_test_users(self):
        """清理所有自动创建的测试账号"""
        if not self._created_users:
            return

        # 确保是管理员
        current_token = self.client.token
        self.login_as_admin()

        for user in self._created_users:
            uid = user.get("id")
            if uid:
                self.client.delete(f"/users/{uid}")

        self._created_users.clear()

        # 恢复之前的登录状态
        if current_token:
            self.client.token = current_token

    # ---------- 权限相关 ----------

    def get_current_permissions(self) -> List[str]:
        """获取当前用户权限列表"""
        if self.client.current_user:
            return self.client.current_user.get("permissions", [])
        return []

    def has_permission(self, perm: str) -> bool:
        """检查当前用户是否有某权限"""
        perms = self.get_current_permissions()
        # 管理员拥有所有权限
        if self.client.current_user and self.client.current_user.get("role") == "ADMIN":
            return True
        return perm in perms

    @property
    def is_admin(self) -> bool:
        return self.client.current_user and self.client.current_user.get("role") == "ADMIN"
