package com.orbaic.email.repositories;

import com.orbaic.email.models.settings.AppSettings;
import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.SQLUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Integer> {

    @Query("SELECT app FROM AppSettings app WHERE app.settingName = ?1")
    Optional<AppSettings> findBySettingName(String name);
}
