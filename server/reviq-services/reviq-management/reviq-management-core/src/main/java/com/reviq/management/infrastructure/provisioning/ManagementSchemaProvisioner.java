package com.reviq.management.infrastructure.provisioning;

import com.reviq.multitenancy.provisioning.AbstractSchemaProvisioner;
import org.springframework.stereotype.Component;

@Component
public class ManagementSchemaProvisioner extends AbstractSchemaProvisioner {

    @Override
    public String getSchemaName() {
        return "management";
    }

    @Override
    public String getChangelogPath() {
        return "classpath:db/changelog/db.changelog-master.xml";
    }
}
