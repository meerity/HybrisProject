package com.epam.training.jobs;

import com.epam.training.service.ImportService;
import com.epam.training.service.impl.ImpexImportService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import org.apache.log4j.Logger;

public class DirectoryImpexImportJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(DirectoryImpexImportJob.class);

    private ImportService importService;
    private String impexDirectory;

    @Override
    public PerformResult perform(final CronJobModel cronJob) {
        try {
            boolean importSuccess = importService.importData(impexDirectory);

            if (importSuccess) {
                LOG.info("All impex files were imported successfully");
                return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
            } else {
                LOG.error("Some or all impex files failed to import");
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
            }

        } catch (Exception e) {
            LOG.error("Critical error during impex import", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }


    public void setImportService(ImpexImportService importService) {
        this.importService = importService;
    }

    public void setImpexDirectory(String impexDirectory) {
        this.impexDirectory = impexDirectory;
    }
}
