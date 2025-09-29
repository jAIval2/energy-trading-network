// Simple logger for tracking oracle operations

const logger = {
    info: (message, data = null) => {
        const timestamp = new Date().toISOString();
        console.log(`[${timestamp}] INFO: ${message}`);
        if (data) console.log(JSON.stringify(data, null, 2));
    },
    
    error: (message, error = null) => {
        const timestamp = new Date().toISOString();
        console.error(`[${timestamp}] ERROR: ${message}`);
        if (error) {
            console.error('Error details:', error.message);
            if (error.stack) console.error(error.stack);
        }
    },
    
    success: (message, data = null) => {
        const timestamp = new Date().toISOString();
        console.log(`[${timestamp}] SUCCESS: ${message}`);
        if (data) console.log(JSON.stringify(data, null, 2));
    }
};

module.exports = logger;
