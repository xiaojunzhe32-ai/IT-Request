package com.itop.core.repository;

import com.itop.core.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    List<RequestComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    /** 请求人可见的留言（公开 + 系统），过滤掉内部工作备注 */
    List<RequestComment> findByTicketIdAndLogTypeNotOrderByCreatedAtAsc(Long ticketId, RequestComment.LogType excludeLogType);
}
