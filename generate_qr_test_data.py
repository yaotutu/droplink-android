#!/usr/bin/env python3
"""
二维码登录测试数据生成器

用法：
    python generate_qr_test_data.py

输出：
    - 打印带有当前时间戳的 JSON 数据
    - 可以复制到在线二维码生成器（如 https://www.qr-code-generator.com/）
"""

import time
import json

def generate_qr_data(
    gotify_url="http://111.228.1.24:2345",
    app_token="A1B2C3D4E5F6G7H8",
    client_token="X9Y8Z7W6V5U4T3S2",
    server_name="测试服务器"
):
    """生成二维码登录数据"""
    current_timestamp = int(time.time() * 1000)

    qr_data = {
        "version": "1.0",
        "type": "droplink_qr_login",
        "timestamp": current_timestamp,
        "data": {
            "gotifyServerUrl": gotify_url,
            "appToken": app_token,
            "clientToken": client_token,
            "serverName": server_name
        }
    }

    return qr_data

def main():
    print("=" * 60)
    print("Droplink 二维码登录测试数据生成器")
    print("=" * 60)
    print()

    # 生成有效的二维码数据
    print("【1】有效的二维码数据（5 分钟有效期）：")
    print("-" * 60)
    valid_data = generate_qr_data()
    print(json.dumps(valid_data, ensure_ascii=False, indent=2))
    print()

    # 生成过期的二维码数据
    print("【2】过期的二维码数据（用于测试过期验证）：")
    print("-" * 60)
    expired_timestamp = int(time.time() * 1000) - (6 * 60 * 1000)  # 6 分钟前
    expired_data = generate_qr_data()
    expired_data["timestamp"] = expired_timestamp
    print(json.dumps(expired_data, ensure_ascii=False, indent=2))
    print()

    # 生成错误类型的二维码数据
    print("【3】错误类型的二维码数据（用于测试类型验证）：")
    print("-" * 60)
    wrong_type_data = generate_qr_data()
    wrong_type_data["type"] = "other_app_qr_code"
    print(json.dumps(wrong_type_data, ensure_ascii=False, indent=2))
    print()

    # 生成缺少字段的二维码数据
    print("【4】缺少必需字段的二维码数据（用于测试字段验证）：")
    print("-" * 60)
    missing_fields_data = generate_qr_data()
    del missing_fields_data["data"]["appToken"]
    del missing_fields_data["data"]["clientToken"]
    print(json.dumps(missing_fields_data, ensure_ascii=False, indent=2))
    print()

    # 生成无效 Token 的二维码数据
    print("【5】无效 Token 的二维码数据（用于测试 Token 验证）：")
    print("-" * 60)
    invalid_token_data = generate_qr_data(
        app_token="INVALID_APP_TOKEN",
        client_token="INVALID_CLIENT_TOKEN"
    )
    print(json.dumps(invalid_token_data, ensure_ascii=False, indent=2))
    print()

    print("=" * 60)
    print("提示：")
    print("1. 复制上面的 JSON 数据到在线二维码生成器")
    print("2. 推荐使用：https://www.qr-code-generator.com/")
    print("3. 或使用：https://www.qrcode-monkey.com/")
    print("4. 生成二维码后，使用 Droplink 应用扫描测试")
    print("=" * 60)

if __name__ == "__main__":
    main()
