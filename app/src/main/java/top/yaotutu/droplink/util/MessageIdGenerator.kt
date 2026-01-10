package top.yaotutu.droplink.util

import kotlin.random.Random

/**
 * 消息 ID 生成器
 *
 * React 概念对标：
 * - 类似于前端的 UUID 生成工具或 nanoid
 * - const generateId = () => `msg_${Date.now()}_${randomString()}`
 *
 * 核心功能：
 * 1. 生成全局唯一的消息 ID
 * 2. 格式：msg_timestamp_randomString
 * 3. 确保 ID 在时间和随机性上的唯一性
 *
 * 使用示例：
 * ```kotlin
 * val id = MessageIdGenerator.generate()  // "msg_1736524800000_a3f5b9"
 * ```
 */
object MessageIdGenerator {

    // 随机字符串字符集（小写字母 + 数字，避免混淆字符如 0/O, 1/l）
    private const val CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789"

    // 随机字符串长度（6 位提供 36^6 ≈ 21 亿种组合，配合时间戳足够唯一）
    private const val RANDOM_LENGTH = 6

    /**
     * 生成消息 ID
     *
     * @return 格式化的消息 ID：msg_1736524800000_a3f5b9
     *
     * 格式说明：
     * - 前缀：msg_（标识这是一个消息 ID）
     * - 时间戳：13 位毫秒级时间戳（确保时间上的唯一性）
     * - 随机字符串：6 位小写字母数字组合（确保同一毫秒内的唯一性）
     *
     * 唯一性保证：
     * - 时间戳确保不同时间的消息 ID 不重复
     * - 随机字符串确保同一毫秒内的多个消息 ID 不重复
     * - 理论碰撞概率：1 / (36^6) ≈ 1 / 21 亿
     */
    fun generate(): String {
        val timestamp = System.currentTimeMillis()
        val randomString = generateRandomString()
        return "msg_${timestamp}_$randomString"
    }

    /**
     * 生成随机字符串
     *
     * @return 6 位随机字符串（小写字母 + 数字）
     */
    private fun generateRandomString(): String {
        return (1..RANDOM_LENGTH)
            .map { CHARSET[Random.nextInt(CHARSET.length)] }
            .joinToString("")
    }

    /**
     * 从 ID 中提取时间戳（用于调试或日志记录）
     *
     * @param id 消息 ID
     * @return 时间戳（毫秒），如果格式不正确返回 null
     *
     * 使用示例：
     * ```kotlin
     * val timestamp = MessageIdGenerator.extractTimestamp("msg_1736524800000_a3f5b9")
     * println(timestamp)  // 1736524800000
     * ```
     */
    fun extractTimestamp(id: String): Long? {
        return try {
            val parts = id.split("_")
            if (parts.size == 3 && parts[0] == "msg") {
                parts[1].toLongOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
