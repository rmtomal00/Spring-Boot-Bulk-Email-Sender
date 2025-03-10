package com.orbaic.email.models.emailDataManage.emailTempate;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateHelperModel {
    @Id
    int id;
    @Column(name = "templateName")
    String templateName;
}
