package com.orbaic.email.models.emailDataManage.emailTempate;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "emailTemplates",
        indexes = {
                @Index(name = "idx_templateName", columnList = "templateName")
        }
)
public class EmailTemplatesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "templateName", nullable = false, unique = true, length = 255)
    private String templateName;

    @Column(name = "templateValue", nullable = false, columnDefinition = "TEXT")
    private String templateValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdDate", nullable = false, updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updatedDate")
    private Date updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
        updatedDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date();
    }
}
