"""
HTTP 客户端封装 - 统一的请求发送和响应处理
"""

import time
import json
from typing import Optional, Dict, Any, Tuple
import requests


class HttpClient:
    """HTTP 客户端，封装请求发送、认证头、重试等"""

    def __init__(self, config):
        self.config = config
        self.session = requests.Session()
        self.session.headers.update({
            "Content-Type": "application/json",
            "Accept": "application/json"
        })
        self._token: Optional[str] = None
        self._current_user: Optional[Dict] = None

    # ---------- Token 管理 ----------

    @property
    def token(self) -> Optional[str]:
        return self._token

    @token.setter
    def token(self, value: Optional[str]):
        self._token = value
        if value:
            self.session.headers["Authorization"] = f"Bearer {value}"
        else:
            self.session.headers.pop("Authorization", None)

    @property
    def current_user(self) -> Optional[Dict]:
        return self._current_user

    @current_user.setter
    def current_user(self, value: Optional[Dict]):
        self._current_user = value

    def clear_auth(self):
        """清除认证信息"""
        self.token = None
        self._current_user = None

    # ---------- 核心请求方法 ----------

    def _build_url(self, path: str) -> str:
        """构建完整 URL"""
        if path.startswith("http"):
            return path
        base = self.config.api_base_url
        path = path.lstrip("/")
        return f"{base}/{path}"

    def request(
        self,
        method: str,
        path: str,
        data: Any = None,
        params: Dict = None,
        headers: Dict = None,
        timeout: int = None,
        expected_status: int = None,
    ) -> Tuple[int, Any, Optional[str]]:
        """
        发送 HTTP 请求
        返回: (status_code, response_data, error_message)
        """
        url = self._build_url(path)
        if timeout is None:
            timeout = self.config.timeout

        req_headers = {}
        if headers:
            req_headers.update(headers)

        try:
            response = self.session.request(
                method=method.upper(),
                url=url,
                json=data if data is not None else None,
                params=params,
                headers=req_headers if req_headers else None,
                timeout=timeout,
            )

            # 尝试解析 JSON
            try:
                resp_data = response.json()
            except (json.JSONDecodeError, ValueError):
                resp_data = response.text

            # 检查预期状态码
            if expected_status is not None and response.status_code != expected_status:
                return response.status_code, resp_data, f"预期状态码 {expected_status}，实际 {response.status_code}"

            return response.status_code, resp_data, None

        except requests.exceptions.Timeout:
            return 0, None, f"请求超时（{timeout}s）: {method} {url}"
        except requests.exceptions.ConnectionError as e:
            return 0, None, f"连接失败: {str(e)}"
        except Exception as e:
            return 0, None, f"请求异常: {str(e)}"

    def get(self, path: str, params: Dict = None, **kwargs) -> Tuple[int, Any, Optional[str]]:
        return self.request("GET", path, params=params, **kwargs)

    def post(self, path: str, data: Any = None, **kwargs) -> Tuple[int, Any, Optional[str]]:
        return self.request("POST", path, data=data, **kwargs)

    def put(self, path: str, data: Any = None, **kwargs) -> Tuple[int, Any, Optional[str]]:
        return self.request("PUT", path, data=data, **kwargs)

    def delete(self, path: str, **kwargs) -> Tuple[int, Any, Optional[str]]:
        return self.request("DELETE", path, **kwargs)

    # ---------- 页面访问 ----------

    def visit_page(self, path: str) -> Tuple[int, Optional[str]]:
        """
        访问前端页面（返回 HTML）
        返回: (status_code, error_message)
        """
        url = f"{self.config.base_url.rstrip('/')}{path}"
        try:
            response = self.session.get(url, timeout=self.config.timeout)
            return response.status_code, None
        except Exception as e:
            return 0, str(e)

    # ---------- 便捷方法 ----------

    def is_success(self, status_code: int, resp_data: Any) -> bool:
        """判断 API 响应是否成功（code == 200）"""
        if isinstance(resp_data, dict) and resp_data.get("code") == 200:
            return True
        return status_code == 200

    def get_data(self, resp_data: Any) -> Any:
        """从 API 响应中提取 data 字段"""
        if isinstance(resp_data, dict):
            return resp_data.get("data")
        return resp_data

    def close(self):
        """关闭会话"""
        self.session.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()
