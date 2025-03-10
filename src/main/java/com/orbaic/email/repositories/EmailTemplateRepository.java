package com.orbaic.email.repositories;

import com.orbaic.email.models.emailDataManage.emailTempate.EmailTemplateHelperModel;
import com.orbaic.email.models.emailDataManage.emailTempate.EmailTemplatesModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplatesModel, Integer> {
    @Query("SELECT new com.orbaic.email.models.emailDataManage.emailTempate.EmailTemplateHelperModel(e.id, e.templateName) FROM EmailTemplatesModel e")
    List<EmailTemplateHelperModel> findAllTemplates();
}
