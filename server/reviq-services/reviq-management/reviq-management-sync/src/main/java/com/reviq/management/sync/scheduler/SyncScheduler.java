package com.reviq.management.sync.scheduler;

import com.reviq.management.sync.service.SyncOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final SyncOrchestrator syncOrchestrator;

    @Scheduled(cron = "${sync.schedule.full:0 0 2 * * *}")
    @SchedulerLock(name = "fullErpSync", lockAtLeastFor = "5m", lockAtMostFor = "2h")
    public void scheduledFullSync() {
        log.info("Scheduled full ERP sync triggered");
        syncOrchestrator.runFullSync();
    }
}
