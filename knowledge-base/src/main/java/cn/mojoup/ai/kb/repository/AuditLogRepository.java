package cn.mojoup.ai.kb.repository;

import cn.mojoup.ai.kb.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository
 * 
 * @author matt
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> findByUserIdOrderByOperationTimeDesc(String userId);

    /**
     * 根据操作类型查询审计日志
     */
    List<AuditLog> findByOperationTypeOrderByOperationTimeDesc(String operationType);

    /**
     * 根据资源ID查询审计日志
     */
    List<AuditLog> findByResourceIdOrderByOperationTimeDesc(String resourceId);

    /**
     * 根据时间范围查询审计日志
     */
    List<AuditLog> findByOperationTimeBetweenOrderByOperationTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和时间范围查询审计日志
     */
    List<AuditLog> findByUserIdAndOperationTimeBetweenOrderByOperationTimeDesc(
            String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.operationTime BETWEEN :startTime AND :endTime")
    Long countByUserIdAndTimeBetween(@Param("userId") String userId, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作类型次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operationType = :operationType AND a.operationTime BETWEEN :startTime AND :endTime")
    Long countByOperationTypeAndTimeBetween(@Param("operationType") String operationType, 
                                          @Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的审计日志
     */
    void deleteByOperationTimeBefore(LocalDateTime cutoffTime);
} 