package com.orbaic.email.models.settings.settingModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDefaultSetting {
    private Boolean status;
    private Integer limit;
}
