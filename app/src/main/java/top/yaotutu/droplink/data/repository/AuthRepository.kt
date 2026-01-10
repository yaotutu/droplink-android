package top.yaotutu.droplink.data.repository

import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.data.model.ValidationResult

/**
 * 认证仓库接口
 * 负责处理登录、注册等认证相关的业务逻辑
 */
interface AuthRepository {
    /**
     * 验证邮箱格式
     */
    fun validateEmail(email: String): ValidationResult

    /**
     * 验证验证码格式
     */
    fun validateVerificationCode(code: String): ValidationResult

    /**
     * 发送验证码到指定邮箱
     * @return Result<Unit> 成功返回 Unit，失败返回异常
     */
    suspend fun sendVerificationCode(email: String): Result<Unit>

    /**
     * 使用验证码统一验证（注册/登录）
     * 后端会自动判断是注册还是登录
     * @return Result<User> 成功返回 User，失败返回异常
     */
    suspend fun verify(email: String, code: String): Result<User>
}
