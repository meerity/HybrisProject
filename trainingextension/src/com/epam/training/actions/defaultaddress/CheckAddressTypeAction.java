package com.epam.training.actions.defaultaddress;

import com.epam.training.model.AddressDefaultProcessModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.log4j.Logger;

import java.util.Set;

public class CheckAddressTypeAction extends AbstractAction<AddressDefaultProcessModel> {

    private static final Logger LOG = Logger.getLogger(CheckAddressTypeAction.class);

    @Override
    public String execute(AddressDefaultProcessModel model) {
        try {
            AddressModel address = model.getAddress();

            if (address.getBillingAddress()) {
                return "BILLING";
            } else if (address.getShippingAddress()) {
                return "SHIPMENT";
            } else {
                LOG.warn("No valid address for address" + address.getPk());
                return "ERROR";
            }
        } catch (Exception e) {
            LOG.error(e);
            return "ERROR";
        }
    }

    @Override
    public Set<String> getTransitions() {
        return Set.of("BILLING", "SHIPMENT", "ERROR");
    }

}
