package com.reviq.ai.infrastructure.provisioning;

import com.reviq.multitenancy.provisioning.AbstractSchemaProvisioner;
import org.springframework.stereotype.Component;

@Component
public class AiSchemaProvisioner extends AbstractSchemaProvisioner {

    @Override
    public String getSchemaName() {
        return "ai";
    }

    @Override
    public String getChangelogPath() {
        return "classpath:db/changelog/db.changelog-master.xml";
    }
}
