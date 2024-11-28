package com.epam.training.listeners;

import com.epam.training.model.AddressDefaultProcessModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.event.events.AfterItemCreationEvent;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

public class AddressAfterSaveListener extends AbstractEventListener<AfterItemCreationEvent> {

    private static final Logger LOG = Logger.getLogger(AddressAfterSaveListener.class);
    private static final String PROCESS_DEFINITION_NAME = "AddressDefaultProcess";

    private BusinessProcessService businessProcessService;
    private ModelService modelService;

    @Override
    protected void onEvent(AfterItemCreationEvent event) {
        if (event != null && event.getSource() != null) {

            final Object o = modelService.get(event.getSource());

            if (o instanceof AddressModel address) {
                try {
                    launchAddressDefaultProcess(address);
                } catch (Exception e) {
                    LOG.error("Failed to launch address process", e);
                }
            }
        }
    }

    private void launchAddressDefaultProcess(AddressModel address) {
        String processCode = "addressDefaultProcess-" + System.currentTimeMillis();

        AddressDefaultProcessModel process = businessProcessService.createProcess(
                processCode,
                PROCESS_DEFINITION_NAME
        );
        process.setAddress(address);

        businessProcessService.startProcess(process);
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

}