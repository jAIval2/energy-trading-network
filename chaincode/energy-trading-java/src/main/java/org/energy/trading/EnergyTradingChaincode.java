package org.energy.trading;

import org.hyperledger.fabric.contract.ContractRouter;
import java.util.logging.Logger;

public final class EnergyTradingChaincode {
    private static final Logger logger = Logger.getLogger(EnergyTradingChaincode.class.getName());

    public static void main(final String[] args) {
        try {
            System.out.println("=== Starting Energy Trading Chaincode ===");
            logger.info("Chaincode initialization started");

            // Start the contract router (contracts auto-discovered via @Contract)
            ContractRouter router = new ContractRouter(args);
            logger.info("Chaincode ready, starting router...");
            router.start(args);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to start chaincode: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
