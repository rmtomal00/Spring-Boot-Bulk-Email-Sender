package com.orbaic.email.repositories;

import com.orbaic.email.models.emailDataManage.prepareEmailTask.PrepareEmailTaskModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrepareEmailRepository extends JpaRepository<PrepareEmailTaskModel, Long> {
    @Query("SELECT d FROM PrepareEmailTaskModel d")
    List<PrepareEmailTaskModel> getFirstLimit(Pageable pageable);

}
