package com.orbaic.email.models.settings;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Primary;

@Data
@Table(name = "appSettings",
    indexes = {
        @Index(name = "idx_settingsName", columnList = "settingName")
})
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AppSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "settingName", unique = true, nullable = false, length = 250)
    String settingName;

    @Type(JsonType.class)
    @Column(name = "settingValue", nullable = false, columnDefinition = "JSON")
    Object settingValue;
}
