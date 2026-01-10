package top.yaotutu.droplink.data.model

/**
 * 表单验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
