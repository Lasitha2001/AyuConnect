package com.ayusystem.notice.Repository;

import com.ayusystem.notice.Model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice,Long> {
}
