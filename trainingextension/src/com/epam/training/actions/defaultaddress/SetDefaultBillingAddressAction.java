package com.epam.training.actions.defaultaddress;

import com.epam.training.model.AddressDefaultProcessModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

import java.util.Set;

public class SetDefaultBillingAddressAction extends AbstractAction<AddressDefaultProcessModel> {

    private static final Logger LOG = Logger.getLogger(SetDefaultBillingAddressAction.class);

    private ModelService modelService;

    @Override
    public String execute(AddressDefaultProcessModel model) {
        try {
            AddressModel address = model.getAddress();
            CustomerModel customer = (CustomerModel) address.getOwner();

            customer.setDefaultPaymentAddress(address);
            modelService.save(customer);

            LOG.info("Set default billing address to " + customer.getCustomerID());
            return "OK";
        } catch (ModelSavingException e) {
            LOG.error(e);
            return "NOK";
        }
    }

    @Override
    public Set<String> getTransitions() {
        return Set.of("OK", "NOK");
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
