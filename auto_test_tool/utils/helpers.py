"""
工具函数
"""

import random
import string
import time
from typing import Optional


def random_string(length: int = 8) -> str:
    """生成随机字符串"""
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=length))


def random_username(prefix: str = "autotest_") -> str:
    """生成随机用户名"""
    return f"{prefix}{random_string(8)}"


def random_email(username: str = None) -> str:
    """生成随机邮箱"""
    if username is None:
        username = random_username()
    return f"{username}@test.autotest.local"


def timestamp_ms() -> int:
    """获取当前毫秒时间戳"""
    return int(time.time() * 1000)


def wait(ms: int):
    """等待指定毫秒"""
    time.sleep(ms / 1000)


def truncate(text: str, max_len: int = 200) -> str:
    """截断文本"""
    if len(text) <= max_len:
        return text
    return text[:max_len] + "..."


def safe_get(d: dict, *keys, default=None):
    """安全获取嵌套字典值"""
    for key in keys:
        if isinstance(d, dict):
            d = d.get(key, default)
        else:
            return default
    return d
