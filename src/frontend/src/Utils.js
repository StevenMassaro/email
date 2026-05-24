const dateFormat = new Intl.DateTimeFormat('fr-CA');
const timeFormat = new Intl.DateTimeFormat([], { hour: '2-digit', minute: '2-digit' })

export function formatDate(date) {
    return dateFormat.format(date)+ " " + timeFormat.format(date)
}

export async function fetchWithRetry(url, options = {}, maxRetries = 3) {
    let lastError;
    for (let i = 0; i < maxRetries; i++) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error(`${response.status} ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            lastError = error;
            // If we have retries left, wait a bit before trying again
            if (i < maxRetries - 1) {
                // Wait for 1 second * (attempt number) to avoid hammering the server
                await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
            }
        }
    }
    throw lastError;
}